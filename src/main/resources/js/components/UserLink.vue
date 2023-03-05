<template>
  <div v-if="user">
    <router-link
        :to="`/user/${user.id}`"
        class="px-3"
    >
      <v-avatar
          v-if="user && user.userpic"
          :size="`${size || 36}px`"
      >
        <img
            :src="user.userpic"
            :alt="user.name"
        >
      </v-avatar>

      <v-avatar
          v-else
          :size="`${size || 36}px`"
          color="indigo"
      >
        <v-icon dark>account_circle</v-icon>
      </v-avatar>
      <span>{{ userName }}</span>
    </router-link>
  </div>
  <div v-else>
    <v-avatar
        v-if="user && user.userpic"
        :size="`${size || 36}px`"
    >
      <img
          :src="user.userpic"
          :alt="user.name"
      >
    </v-avatar>

    <v-avatar
        v-else
        :size="`${size || 36}px`"
        color="indigo"
    >
      <v-icon dark>account_circle</v-icon>
    </v-avatar>
  </div>
</template>

<script>
export default {
  name: 'UserLink',
  props: ['user', 'size'],
  computed: {
    userName() {
      return this.user ? this.user.name : 'unknown'
    }
  },
}
</script>

<style scoped>
</style>
<!--
<template>
  <router-link
    :to="`/user/${user.id}`"
    class="px-3"
  >
    <v-avatar
        v-if="user && user.userpic"
        :size="`${size || 36}px`"
    >
      <img
          :src="user.userpic"
          :alt="user.name"
      >
    </v-avatar>

    <v-avatar
        v-else
        :size="`${size || 36}px`"
        color="indigo"
    >
      <v-icon dark>account_circle</v-icon>
    </v-avatar>
    <span>{{userName}}</span>
  </router-link>
</template>

<script>
export default {
  name: "UserLink",
  props: ['user', 'size'],
  computed: {
    userName() {
      return this.user.id ? this.user.name : 'unknown'
    }
  },
}
</script>

<style scoped>

</style>-->
