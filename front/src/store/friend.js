import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'
import Cookies from 'js-cookie'

export const useFriendStore = defineStore('friend', () => {
    const friends = ref([])
    const friendRequests = ref([])
    const isLoading = ref(false)

    async function getFriendsList() {
        try {
            isLoading.value = true
            const res = await axios.get(`${import.meta.env.VITE_API_BASE}/contacts/list`, {
                headers: {
                    Authorization: `Bearer ${Cookies.get('token')}`,
                },
            })

            if (res.data.code === "200") {
                friends.value = res.data.data
            }
        } catch (err) {
            console.error('Failed to get friends list:', err)
            throw err
        } finally {
            isLoading.value = false
        }
    }

    async function getFriendRequests() {
        try {
            const res = await axios.get(`${import.meta.env.VITE_API_BASE}/contacts/requests`, {
                headers: {
                    Authorization: `Bearer ${Cookies.get('token')}`,
                },
            })

            if (res.data.code === "200") {
                friendRequests.value = res.data.data
            }
        } catch (err) {
            console.error('Failed to get friend requests:', err)
            throw err
        }
    }

    function updateFriendStatus(friendId, isOnline) {
        const friend = friends.value.find(f => f.id === friendId)
        if (friend) {
            friend.online = isOnline
        }
    }

    function addFriendRequest(request) {
        friendRequests.value.push(request)
    }

    function removeFriendRequest(requestId) {
        friendRequests.value = friendRequests.value.filter(r => r.id !== requestId)
    }

    function addFriend(friend) {
        friends.value.push(friend)
    }

    function removeFriend(friendId) {
        friends.value = friends.value.filter(f => f.id !== friendId)
    }

    return {
        friends,
        friendRequests,
        isLoading,
        getFriendsList,
        getFriendRequests,
        updateFriendStatus,
        addFriendRequest,
        removeFriendRequest,
        addFriend,
        removeFriend,
    }
})