<template>
  <div class="card-container">
    <div class="card-item">
      <div class="img-container">
        <img class="img-item" src="@/assets/images/bg-3.jpg" alt="" />
      </div>
      <div class="card-info">
        <div class="card-booked"></div>
        <div class="card-time">
          <p class="card-time-y-m-d">2026-08-31</p>
          <p class="card-time-h-m">12:00</p>
        </div>
        <div class="card-comments"></div>
        <div class="user-avatar">
          <img
            class="user-avatar-img"
            src="@/assets/avatar/user-avatar-6.jpg"
            alt=""
          />
        </div>
        <div class="user-name">
          <p class="user-name-text">John Doe</p>
        </div>
      </div>
      <div class="card-titl-container">
        <h2 class="card-titl">
          Lorem ipsum dolor sit amet consectetur adipisicing elit.
        </h2>
      </div>

      <div class="card-description-container">
        <p class="card-description">
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Quisquam,
          quos. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Quisquam, quos.Lorem ipsum dolor sit amet consectetur adipisicing
          elit. Quisquam, quos.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";

const msg = ref("");
</script>

<style scoped>
.card-container {
  background-color: rgb(255, 255, 255);
}

.card-item {
  padding: 20px;
  width: 600px;
  height: auto;

  background-color: rgb(255, 255, 255);
}

.img-item {
  width: 450px;
  height: auto;
}

.user-avatar-img {
  width: 64px;
  height: 64px;
  border-radius: 50%;
}

.user-name-text {
  font-size: 16px;
}
</style>
