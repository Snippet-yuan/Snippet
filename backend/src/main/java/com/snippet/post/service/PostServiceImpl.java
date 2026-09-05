package com.snippet.post.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippet.common.exception.BusinessException;
import com.snippet.post.dto.CreatePostRequest;
import com.snippet.post.dto.CreateCommentRequest;
import com.snippet.post.dto.PostDetailResponse;
import com.snippet.post.dto.PostCommentResponse;
import com.snippet.post.dto.PostFavoriteStatusResponse;
import com.snippet.post.dto.PostLikeStatusResponse;
import com.snippet.post.dto.PublishRequest;
import com.snippet.post.dto.SaveDraftRuquest;
import com.snippet.post.dto.UpdatePostRequest;
import com.snippet.post.entity.Post;
import com.snippet.post.entity.PostComment;
import com.snippet.post.entity.PostDraft;
import com.snippet.post.entity.PostRevision;
import com.snippet.post.validator.PostContentValidator;
import com.snippet.post.mapper.PostMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.List;

/**
 * 帖子核心业务。创建帖子时同时创建帖子主记录和初始草稿。
 */
@Service
public class PostServiceImpl implements PostService {

    private static final int TITLE_MAX_LENGTH = 200;
    private static final int DESCRIPTION_MAX_LENGTH = 5000;
    private static final int COMMENT_MAX_LENGTH = 2000;
    private static final int DEFAULT_COMMENT_PAGE_SIZE = 20;
    private static final int MAX_COMMENT_PAGE_SIZE = 100;
    private static final int MAX_COMMENT_OFFSET = 10000;
    private static final int INITIAL_SCHEMA_VERSION = 1;
    private static final int INITIAL_DRAFT_VERSION = 0;
    private static final int SLUG_MAX_LENGTH = 120;
    private static final String DRAFT_STATUS = "DRAFT";
    private static final String PUBLISHED_STATUS = "PUBLISHED";
    private static final String INITIAL_CONTENT_JSON = "{\"type\":\"doc\",\"children\":[]}";

    private final PostMapper postMapper;
    private final ObjectMapper objectMapper;
    private final PostContentValidator contentValidator;

    public PostServiceImpl(PostMapper postMapper, ObjectMapper objectMapper) {
        this.postMapper = postMapper;
        this.objectMapper = objectMapper;
        this.contentValidator = new PostContentValidator();
    }

    @Override
    @Transactional
    public PostDetailResponse createPost(Long ownerId, CreatePostRequest request) {
        validateOwnerId(ownerId);
        String title = normalizeTitle(request);

        Post post = new Post();
        post.setOwnerId(ownerId);
        post.setTitle(title);
        post.setSlug(generateSlug());
        post.setStatus(DRAFT_STATUS);

        try {
            int postRows = postMapper.insertPost(post);
            if (postRows != 1 || post.getId() == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "帖子创建失败"
                );
            }

            PostDraft draft = new PostDraft();
            draft.setPostId(post.getId());
            draft.setContentJson(INITIAL_CONTENT_JSON);
            draft.setSchemaVersion(INITIAL_SCHEMA_VERSION);
            draft.setVersion(INITIAL_DRAFT_VERSION);
            draft.setUpdatedBy(ownerId);

            int draftRows = postMapper.insertInitialDraft(draft);
            if (draftRows != 1) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "初始草稿创建失败"
                );
            }
        } catch (DuplicateKeyException exception) {
            throw exception;
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "帖子创建失败"
            );
        }

        Post savedPost = postMapper.selectByIdAndOwnerId(post.getId(), ownerId);
        PostDraft savedDraft = postMapper.selectDraftByPostId(post.getId());
        if (savedPost == null || savedDraft == null) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "帖子创建结果读取失败"
            );
        }

        return toResponse(savedPost, savedDraft);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(Long ownerId, Long postId) {
        validateOwnerId(ownerId);
        validatePostId(postId);

        try {
            Post post = postMapper.selectByIdAndOwnerId(postId, ownerId);
            if (post == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "帖子不存在");
            }

            PostDraft draft = postMapper.selectDraftByPostId(postId);
            if (draft == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "帖子草稿读取失败"
                );
            }

            return toResponse(post, draft);
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "帖子详情读取失败"
            );
        }
    }

    @Override
    @Transactional
    public PostDetailResponse saveDraft(Long ownerId, Long postId, SaveDraftRuquest request) {
        validateOwnerId(ownerId);
        validatePostId(postId);
        validateDraftRequest(request);
        contentValidator.validate(request.getContent(), request.getSchemaVersion());

        String contentJson;
        try {
            contentJson = objectMapper.writeValueAsString(request.getContent());
        } catch (JsonProcessingException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "正文格式不合法");
        }

        try {
            Post post = postMapper.selectByIdAndOwnerId(postId, ownerId);
            if (post == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "帖子不存在");
            }

            PostDraft currentDraft = postMapper.selectDraftByPostId(postId);
            if (currentDraft == null || currentDraft.getVersion() == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "帖子草稿读取失败"
                );
            }
            if (!request.getVersion().equals(currentDraft.getVersion())) {
                throw new BusinessException(HttpStatus.CONFLICT, "草稿版本已过期，请重新加载");
            }

            int updatedRows = postMapper.updateDraftContent(
                    postId,
                    contentJson,
                    request.getSchemaVersion(),
                    request.getVersion(),
                    ownerId
            );
            if (updatedRows != 1) {
                throw new BusinessException(HttpStatus.CONFLICT, "草稿版本已过期，请重新加载");
            }

            int touchedRows = postMapper.touchPostUpdatedAt(postId, ownerId);
            if (touchedRows != 1) {
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "帖子更新时间失败");
            }

            Post savedPost = postMapper.selectByIdAndOwnerId(postId, ownerId);
            PostDraft savedDraft = postMapper.selectDraftByPostId(postId);
            if (savedPost == null || savedDraft == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "草稿保存结果读取失败"
                );
            }
            return toResponse(savedPost, savedDraft);
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "草稿保存失败"
            );
        }
    }

    @Override
    @Transactional
    public PostDetailResponse updatePost(Long ownerId, Long postId, UpdatePostRequest request) {
        validateOwnerId(ownerId);
        validatePostId(postId);
        validateUpdateRequest(request);

        String title = request.getTitle();
        String description = request.getDescription();

        try {
            Post currentPost = postMapper.selectByIdAndOwnerId(postId, ownerId);
            if (currentPost == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "帖子不存在");
            }

            if (title != null) {
                title = normalizeTitleValue(
                        title,
                        "修改后的标题不能为空或只能是空白字符"
                );
            } else {
                title = currentPost.getTitle();
            }

            if (description != null) {
                description = normalizeDescription(description);
            } else {
                description = currentPost.getDescription();
            }

            int updatedRows = postMapper.updatePostBasicInfo(
                    postId,
                    ownerId,
                    title,
                    description
            );
            if (updatedRows != 1) {
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "帖子已发生变化，请重试"
                );
            }

            Post savedPost = postMapper.selectByIdAndOwnerId(postId, ownerId);
            PostDraft savedDraft = postMapper.selectDraftByPostId(postId);
            if (savedPost == null || savedDraft == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "帖子修改结果读取失败"
                );
            }
            return toResponse(savedPost, savedDraft);
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "帖子修改失败"
            );
        }
    }

    @Override
    @Transactional
    public void deletePost(Long ownerId, Long postId) {
        validateOwnerId(ownerId);
        validatePostId(postId);

        try {
            Post post = postMapper.selectByIdAndOwnerId(postId, ownerId);
            if (post == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "帖子不存在");
            }

            postMapper.clearPublishedRevisionId(postId, ownerId);
            postMapper.deletePostImages(postId);
            postMapper.deletePostLikes(postId);
            postMapper.deletePostFavorites(postId);
            postMapper.deletePostShares(postId);
            postMapper.deletePostComments(postId);
            postMapper.deletePostRevisions(postId);
            postMapper.deletePostDraft(postId);

            int deletedRows = postMapper.deletePost(postId, ownerId);
            if (deletedRows != 1) {
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "帖子已发生变化，请重试"
                );
            }
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "帖子删除失败"
            );
        }
    }

    @Override
    @Transactional
    public PostDetailResponse publishPost(Long ownerId, Long postId, PublishRequest request) {
        validateOwnerId(ownerId);
        validatePostId(postId);
        validatePublishRequest(request);

        try {
            Post post = postMapper.selectByIdAndOwnerId(postId, ownerId);
            if (post == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "帖子不存在");
            }

            PostDraft draft = postMapper.selectDraftByPostId(postId);
            if (draft == null || draft.getVersion() == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "帖子草稿读取失败"
                );
            }
            if (!request.getExpectedVersion().equals(draft.getVersion())) {
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "草稿版本已过期，请重新加载"
                );
            }

            if (!StringUtils.hasText(draft.getContentJson())) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "帖子草稿内容读取失败"
                );
            }

            JsonNode content;
            try {
                content = objectMapper.readTree(draft.getContentJson());
            } catch (JsonProcessingException exception) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "帖子草稿内容读取失败"
                );
            }
            contentValidator.validate(content, draft.getSchemaVersion());

            Integer latestRevisionNo = postMapper.selectLatestRevisionNo(postId);
            int nextRevisionNo;
            if (latestRevisionNo == null) {
                nextRevisionNo = 1;
            } else if (latestRevisionNo == Integer.MAX_VALUE) {
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "历史版本数量已达到上限"
                );
            } else {
                nextRevisionNo = latestRevisionNo + 1;
            }

            PostRevision revision = new PostRevision();
            revision.setPostId(postId);
            revision.setRevisionNo(nextRevisionNo);
            revision.setContentJson(draft.getContentJson());
            revision.setSchemaVersion(draft.getSchemaVersion());
            revision.setCreatedBy(ownerId);

            int revisionRows = postMapper.insertPostRevision(revision);
            if (revisionRows != 1 || revision.getId() == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "文章历史版本创建失败"
                );
            }

            int publishedRows = postMapper.publishPost(
                    postId,
                    ownerId,
                    revision.getId()
            );
            if (publishedRows != 1) {
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "帖子已发生变化，请重试"
                );
            }

            Post savedPost = postMapper.selectByIdAndOwnerId(postId, ownerId);
            PostDraft savedDraft = postMapper.selectDraftByPostId(postId);
            if (savedPost == null || savedDraft == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "帖子发布结果读取失败"
                );
            }
            return toResponse(savedPost, savedDraft);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "历史版本已发生冲突，请重试"
            );
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "帖子发布失败"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailResponse getPublicPost(String slug) {
        String normalizedSlug = normalizeSlug(slug);

        try {
            Post post = postMapper.selectPublishedBySlug(normalizedSlug);
            if (post == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "公开文章不存在");
            }
            if (!PUBLISHED_STATUS.equals(post.getStatus())
                    || post.getPublishedRevisionId() == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "公开文章发布状态无效"
                );
            }

            PostRevision revision = postMapper.selectRevisionById(
                    post.getPublishedRevisionId()
            );
            if (revision == null
                    || !post.getId().equals(revision.getPostId())
                    || revision.getRevisionNo() == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "公开文章版本读取失败"
                );
            }

            return toPublicResponse(post, revision);
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "公开文章读取失败"
            );
        }
    }

    @Override
    @Transactional
    public PostLikeStatusResponse likePost(Long userId, Long postId) {
        validateOwnerId(userId);
        validatePostId(postId);

        try {
            ensurePublishedPost(postId);
            if (postMapper.existsLike(postId, userId)) {
                return new PostLikeStatusResponse(true);
            }

            try {
                int insertedRows = postMapper.insertLike(postId, userId);
                if (insertedRows != 1) {
                    throw new BusinessException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "点赞失败"
                    );
                }
            } catch (DuplicateKeyException exception) {
                // 并发重复请求命中唯一约束时，目标状态已经是“已点赞”。
            }
            return new PostLikeStatusResponse(true);
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "点赞失败"
            );
        }
    }

    @Override
    @Transactional
    public PostLikeStatusResponse unlikePost(Long userId, Long postId) {
        validateOwnerId(userId);
        validatePostId(postId);

        try {
            ensurePublishedPost(postId);
            int deletedRows = postMapper.deleteLike(postId, userId);
            if (deletedRows < 0 || deletedRows > 1) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "取消点赞失败"
                );
            }
            return new PostLikeStatusResponse(false);
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "取消点赞失败"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PostLikeStatusResponse getLikeStatus(Long userId, Long postId) {
        validateOwnerId(userId);
        validatePostId(postId);

        try {
            ensurePublishedPost(postId);
            return new PostLikeStatusResponse(
                    postMapper.existsLike(postId, userId)
            );
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "点赞状态读取失败"
            );
        }
    }

    @Override
    @Transactional
    public PostFavoriteStatusResponse favoritePost(Long userId, Long postId) {
        validateOwnerId(userId);
        validatePostId(postId);

        try {
            ensurePublishedPost(postId);
            if (postMapper.existsFavorite(postId, userId)) {
                return new PostFavoriteStatusResponse(true);
            }

            try {
                int insertedRows = postMapper.insertFavorite(postId, userId);
                if (insertedRows != 1) {
                    throw new BusinessException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "收藏失败"
                    );
                }
            } catch (DuplicateKeyException exception) {
                // 并发重复请求命中唯一约束时，目标状态已经是“已收藏”。
            }
            return new PostFavoriteStatusResponse(true);
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "收藏失败"
            );
        }
    }

    @Override
    @Transactional
    public PostFavoriteStatusResponse unfavoritePost(Long userId, Long postId) {
        validateOwnerId(userId);
        validatePostId(postId);

        try {
            ensurePublishedPost(postId);
            int deletedRows = postMapper.deleteFavorite(postId, userId);
            if (deletedRows < 0 || deletedRows > 1) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "取消收藏失败"
                );
            }
            return new PostFavoriteStatusResponse(false);
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "取消收藏失败"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PostFavoriteStatusResponse getFavoriteStatus(Long userId, Long postId) {
        validateOwnerId(userId);
        validatePostId(postId);

        try {
            ensurePublishedPost(postId);
            return new PostFavoriteStatusResponse(
                    postMapper.existsFavorite(postId, userId)
            );
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "收藏状态读取失败"
            );
        }
    }

    @Override
    @Transactional
    public PostCommentResponse createComment(
            Long userId,
            Long postId,
            CreateCommentRequest request) {
        validateOwnerId(userId);
        validatePostId(postId);
        String content = normalizeCommentContent(request);

        try {
            ensurePublishedPost(postId);

            PostComment comment = new PostComment();
            comment.setPostId(postId);
            comment.setAuthorId(userId);
            comment.setContent(content);

            int insertedRows = postMapper.insertComment(comment);
            if (insertedRows != 1 || comment.getId() == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "评论创建失败"
                );
            }

            PostComment savedComment = postMapper.selectCommentById(comment.getId());
            if (savedComment == null) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "评论创建结果读取失败"
                );
            }
            return toCommentResponse(savedComment);
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "评论创建失败"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostCommentResponse> getPublicComments(
            String slug,
            Integer limit,
            Integer offset) {
        String normalizedSlug = normalizeSlug(slug);
        int normalizedLimit = normalizeCommentLimit(limit);
        int normalizedOffset = normalizeCommentOffset(offset);

        try {
            Post post = postMapper.selectPublishedBySlug(normalizedSlug);
            if (post == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "公开文章不存在");
            }

            List<PostComment> comments = postMapper.selectPublicCommentsBySlug(
                    normalizedSlug,
                    normalizedLimit,
                    normalizedOffset
            );
            if (comments == null || comments.isEmpty()) {
                return List.of();
            }
            return comments.stream()
                    .map(this::toCommentResponse)
                    .toList();
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "公开评论读取失败"
            );
        }
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long postId, Long commentId) {
        validateOwnerId(userId);
        validatePostId(postId);
        validateCommentId(commentId);

        try {
            ensurePublishedPost(postId);

            int deletedRows = postMapper.deleteComment(
                    postId,
                    commentId,
                    userId
            );
            if (deletedRows == 0) {
                throw new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "评论不存在或无权删除"
                );
            }
            if (deletedRows != 1) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "评论删除失败"
                );
            }
        } catch (DataAccessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "评论删除失败"
            );
        }
    }

    private void ensurePublishedPost(Long postId) {
        Post post = postMapper.selectPublishedById(postId);
        if (post == null || !PUBLISHED_STATUS.equals(post.getStatus())) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "帖子不存在或尚未发布"
            );
        }
    }

    private void validatePublishRequest(PublishRequest request) {
        if (request == null
                || request.getExpectedVersion() == null
                || request.getExpectedVersion() < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "发布版本必须为非负数");
        }
    }

    private void validateUpdateRequest(UpdatePostRequest request) {
        if (request == null
                || (request.getTitle() == null && request.getDescription() == null)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "至少修改一项帖子信息");
        }
    }

    private void validateDraftRequest(SaveDraftRuquest request) {
        if (request == null || request.getVersion() == null || request.getVersion() < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "草稿版本必须为非负数");
        }
    }


    private String normalizeTitle(CreatePostRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "创建帖子参数不能为空");
        }
        if (request.getTitle() == null) {
            return null;
        }

        String title = request.getTitle().trim();
        if (!StringUtils.hasText(title)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "标题不能为空或只能是空白字符");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "标题长度不能超过200个字符");
        }
        if (containsControlCharacter(title)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "标题不能包含控制字符");
        }
        return title;
    }

    private boolean containsControlCharacter(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private String normalizeTitleValue(String rawTitle, String blankMessage) {
        String title = rawTitle.trim();
        if (!StringUtils.hasText(title)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, blankMessage);
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "标题长度不能超过200个字符");
        }
        if (containsControlCharacter(title)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "标题不能包含控制字符");
        }
        return title;
    }

    private String normalizeDescription(String rawDescription) {
        String description = rawDescription.trim();
        if (!StringUtils.hasText(description)) {
            return null;
        }
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "文章描述长度不能超过5000个字符"
            );
        }
        if (containsDisallowedControlCharacter(description)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "文章描述包含非法控制字符");
        }
        return description;
    }

    private boolean containsDisallowedControlCharacter(String value) {
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (Character.isISOControl(character)
                    && character != 10
                    && character != 13
                    && character != 9) {
                return true;
            }
        }
        return false;
    }

    private String generateSlug() {
        return "post-" + UUID.randomUUID().toString().replace("-", "");
    }

    private String normalizeSlug(String rawSlug) {
        if (!StringUtils.hasText(rawSlug)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "公开标识不能为空");
        }

        String slug = rawSlug.trim();
        if (slug.length() > SLUG_MAX_LENGTH) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "公开标识长度不能超过120个字符");
        }
        if (containsControlCharacter(slug)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "公开标识不能包含控制字符");
        }
        return slug;
    }

    private void validateOwnerId(Long ownerId) {
        if (ownerId == null || ownerId <= 0) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED,
                    "访问凭证中的用户身份无效"
            );
        }
    }

    private void validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "帖子ID必须为正数");
        }
    }

    private void validateCommentId(Long commentId) {
        if (commentId == null || commentId <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "评论ID必须为正数");
        }
    }

    private String normalizeCommentContent(CreateCommentRequest request) {
        if (request == null || request.getContent() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "评论内容不能为空");
        }

        String rawContent = request.getContent();
        if (containsDisallowedControlCharacter(rawContent)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "评论内容包含非法控制字符"
            );
        }

        String content = rawContent.trim();
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "评论内容不能为空");
        }
        if (content.length() > COMMENT_MAX_LENGTH) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "评论内容长度不能超过2000个字符"
            );
        }
        return content;
    }

    private int normalizeCommentLimit(Integer limit) {
        int normalizedLimit = limit == null ? DEFAULT_COMMENT_PAGE_SIZE : limit;
        if (normalizedLimit <= 0 || normalizedLimit > MAX_COMMENT_PAGE_SIZE) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "评论分页大小必须在1到100之间"
            );
        }
        return normalizedLimit;
    }

    private int normalizeCommentOffset(Integer offset) {
        int normalizedOffset = offset == null ? 0 : offset;
        if (normalizedOffset < 0 || normalizedOffset > MAX_COMMENT_OFFSET) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "评论分页偏移量必须在0到10000之间"
            );
        }
        return normalizedOffset;
    }

    private PostCommentResponse toCommentResponse(PostComment comment) {
        return new PostCommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getAuthorId(),
                comment.getAuthorName(),
                comment.getAuthorAvatarAssetId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

    private PostDetailResponse toResponse(Post post, PostDraft draft) {
        JsonNode content;
        try {
            content = objectMapper.readTree(draft.getContentJson());
        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "帖子草稿内容读取失败"
            );
        }

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getDescription(),
                post.getSlug(),
                post.getStatus(),
                content,
                draft.getSchemaVersion(),
                draft.getVersion(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getPublishedAt()
        );
    }

    private PostDetailResponse toPublicResponse(Post post, PostRevision revision) {
        if (!StringUtils.hasText(revision.getContentJson())) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "公开文章内容读取失败"
            );
        }

        JsonNode content;
        try {
            content = objectMapper.readTree(revision.getContentJson());
            contentValidator.validate(content, revision.getSchemaVersion());
        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "公开文章内容读取失败"
            );
        } catch (BusinessException exception) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "公开文章内容读取失败"
            );
        }

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getDescription(),
                post.getSlug(),
                post.getStatus(),
                content,
                revision.getSchemaVersion(),
                revision.getRevisionNo(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getPublishedAt()
        );
    }
}
