<template>
  <iframe width="560"
          height="315"
          :src="targetSrc"
          title="YouTube video player"
          frameborder="0"
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
          allowfullscreen>
  </iframe>
</template>

<script>
export default {
  name: "YouTube",
  props: ['src'],
  computed: {
    targetSrc() {
      let parts = this.src.split('/');
      let cropUrlLastPart = parts[parts.length - 1].slice(8);
      return `https://www.youtube.com/embed/${cropUrlLastPart}`
    }
  }
}
</script>

<style scoped>

</style>