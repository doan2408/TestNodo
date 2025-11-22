<template>
  <div class="not-found">
    <div class="content">
      <h1>404</h1>
      <h2>Page Not Found</h2>
      <p>Sorry, the page you are looking for does not exist.</p>
      <router-link to="/" class="home-link">Go back to Home</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: "NotFound"
};
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f5f5;
  text-align: center;
  font-family: 'Arial', sans-serif;
}

.content h1 {
  font-size: 6rem;
  margin: 0;
  color: #ff6b6b;
}

.content h2 {
  font-size: 2rem;
  margin: 10px 0;
  color: #333;
}

.content p {
  font-size: 1.2rem;
  margin-bottom: 20px;
  color: #666;
}

.home-link {
  display: inline-block;
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  text-decoration: none;
  border-radius: 5px;
  transition: background 0.3s;
}

.home-link:hover {
  background-color: #0056b3;
}
</style>
