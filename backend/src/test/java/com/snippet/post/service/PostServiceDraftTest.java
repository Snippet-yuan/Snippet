package com.snippet.post.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippet.common.exception.BusinessException;
import com.snippet.post.dto.PostDetailResponse;
import com.snippet.post.dto.SaveDraftRuquest;
import com.snippet.post.entity.Post;
import com.snippet.post.entity.PostDraft;
import com.snippet.post.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceDraftTest {

    @Mock
    private PostMapper postMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void savesTextContentAndIncrementsDraftVersion() throws Exception {
        Post post = post(100L, 42L);
        PostDraft currentDraft = draft(100L, 0, "{\"type\":\"doc\",\"children\":[]}");
        PostDraft savedDraft = draft(100L, 1, "{\"type\":\"doc\",\"children\":[{\"type\":\"paragraph\",\"children\":[{\"text\":\"这是测试正文\"}]}]}");
        when(postMapper.selectByIdAndOwnerId(100L, 42L)).thenReturn(post, post);
        when(postMapper.selectDraftByPostId(100L)).thenReturn(currentDraft, savedDraft);
        when(postMapper.updateDraftContent(100L, savedDraft.getContentJson(), 1, 0, 42L))
                .thenReturn(1);
        when(postMapper.touchPostUpdatedAt(100L, 42L)).thenReturn(1);

        SaveDraftRuquest request = new SaveDraftRuquest();
        request.setContent(objectMapper.readTree(savedDraft.getContentJson()));
        request.setSchemaVersion(1);
        request.setVersion(0);

        PostDetailResponse response = service().saveDraft(42L, 100L, request);

        assertEquals(100L, response.getId());
        assertEquals(1, response.getVersion());
        assertEquals("这是测试正文", response.getContent().at("/children/0/children/0/text").textValue());
        verify(postMapper).updateDraftContent(100L, savedDraft.getContentJson(), 1, 0, 42L);
        verify(postMapper).touchPostUpdatedAt(100L, 42L);
    }

    @Test
    void rejectsStaleDraftVersionBeforeUpdate() throws Exception {
        when(postMapper.selectByIdAndOwnerId(100L, 42L)).thenReturn(post(100L, 42L));
        when(postMapper.selectDraftByPostId(100L))
                .thenReturn(draft(100L, 1, "{\"type\":\"doc\",\"children\":[]}"));

        SaveDraftRuquest request = new SaveDraftRuquest();
        request.setContent(objectMapper.readTree("{\"type\":\"doc\",\"children\":[]}"));
        request.setSchemaVersion(1);
        request.setVersion(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().saveDraft(42L, 100L, request)
        );

        assertEquals(409, exception.getCode());
        verify(postMapper, never()).updateDraftContent(any(), any(), any(), any(), any());
    }

    @Test
    void rejectsInvalidContentStructureBeforeDatabaseRead() throws Exception {
        SaveDraftRuquest request = new SaveDraftRuquest();
        request.setContent(objectMapper.readTree("{\"type\":\"paragraph\",\"children\":[]}"));
        request.setSchemaVersion(1);
        request.setVersion(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().saveDraft(42L, 100L, request)
        );

        assertEquals(400, exception.getCode());
        verify(postMapper, never()).selectByIdAndOwnerId(any(), any());
    }

    @Test
    void rejectsExecutableTextInContent() throws Exception {
        SaveDraftRuquest request = new SaveDraftRuquest();
        request.setContent(objectMapper.readTree("{\"type\":\"doc\",\"children\":[{\"text\":\"<script>alert(1)</script>\"}]}"));
        request.setSchemaVersion(1);
        request.setVersion(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().saveDraft(42L, 100L, request)
        );

        assertEquals(400, exception.getCode());
        verify(postMapper, never()).updateDraftContent(any(), any(), any(), any(), any());
    }

    private Post post(Long postId, Long ownerId) {
        Post post = new Post();
        post.setId(postId);
        post.setOwnerId(ownerId);
        post.setTitle("测试帖子");
        post.setSlug("post-test");
        post.setStatus("DRAFT");
        return post;
    }

    private PostDraft draft(Long postId, int version, String contentJson) {
        PostDraft draft = new PostDraft();
        draft.setPostId(postId);
        draft.setVersion(version);
        draft.setSchemaVersion(1);
        draft.setContentJson(contentJson);
        return draft;
    }

    private PostServiceImpl service() {
        return new PostServiceImpl(postMapper, objectMapper);
    }
}
