<!-- src/components/user/my_page/MyHistory.vue -->
<template>
  <div class="mypage-layout">
    <div class="allcard">
      <div class="intro"><h2>내 정보</h2></div>

      <!-- 프로필 -->
      <div class="image">
        <img
          :src="profileImage || 'https://cdn-icons-png.flaticon.com/512/3135/3135715.png'"
          alt="Profile Image"
          @click="onImageClick"
          class="profile-img"
        />
        <input ref="fileInput" type="file" accept="image/*" @change="onFileChange" style="display:none;" />
      </div>

      <!-- 탭 -->
      <div class="menu-tabs">
        <div class="tab" :class="{ active: $route.name==='MyAccount' }" @click="router.push({ name:'MyAccount' })">계정</div>
        <div class="tab" :class="{ active: $route.name==='MyHistory' }" @click="router.push({ name:'MyHistory' })">예약 내역</div>
        <div class="tab" :class="{ active: $route.name==='MyWishlist' }" @click="router.push({ name:'MyWishlist' })">찜</div>
        <div class="tab" :class="{ active: $route.name==='MyReview' }" @click="router.push({ name:'MyReview' })">리뷰</div>
      </div>

      <!-- 본문 -->
      <div class="my-page2">
        <div v-if="isLoading.history" class="loading">예약 내역을 불러오는 중...</div>
        <div v-else-if="reservations.length===0" class="no-data">예약 내역이 없습니다.</div>

        <div v-else class="reservation-container">
          <div
            v-for="r in reservations"
            :key="r.id"
            class="reservation-card"
            :class="{ active: r.active }"
            @click="toggleReservation(r)"
          >
            <div class="summary">
              <div class="hotel-info">
                <span class="hotel-name">{{ r.hotelName }}</span>
                <span class="dates">{{ formatDate(r.startDate) }} ~ {{ formatDate(r.endDate) }}</span>
              </div>

              <div class="summary-right">
                <button class="btn-detail" @click.stop="goToReservationDetail(r.id)">상세보기</button>
                <span :class="['status-badge', r.status]" style="font-size:.8rem;">{{ r.statusText }}</span>
                <span class="arrow-icon">▼</span>
              </div>
            </div>

            <div class="details">
              <div class="detail-grid">
                <div class="detail-item"><span class="label">객실 타입</span><span class="value">{{ r.roomType }}</span></div>
                <div class="detail-item"><span class="label">인원</span><span class="value">성인 {{ r.adults }}명 / 아동 {{ r.children }}명</span></div>
                <div class="detail-item"><span class="label">객실 수</span><span class="value">{{ r.numRooms }}개</span></div>
              </div>
            </div>
          </div><!-- /card -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'
import { getMy } from '@/api/ReservationApi'
import UserApi from '@/api/UserApi'
import { getAuthUser, setAuthUser, notifyAuthChanged } from '@/utils/auth-storage'

const router = useRouter()
const user = reactive({})
const reservations = ref([])
const isLoading = reactive({ user: true, history: true })
const profileImage = ref('')
const isLoggedIn = ref(false)
const fileInput = ref(null)

const statusMap = { COMPLETED:'예약 완료', CANCELLED:'취소됨', CONFIRMED:'예약 확정', PENDING:'예약 대기', HOLD:'보류' }
const formatDate = s => (s ? new Date(s).toLocaleDateString('ko-KR') : '')
const toggleReservation = r => { r.active = !r.active }
const goToReservationDetail = id => router.push(`/reservations/${id}`)
const onImageClick = () => fileInput.value?.click()
const onFileChange = e => { const f = e?.target?.files?.[0]; if (f) console.log('이미지 업로드:', f.name) }

const checkAuthStatus = () => {
  const userInfo = getAuthUser()
  if (userInfo) {
    isLoggedIn.value = true
    Object.assign(user, userInfo)
    user.passwordLength = user.provider === 'LOCAL' ? 8 : 0
  } else {
    router.push('/login')
  }
}
const fetchUserProfile = async () => {
  isLoading.user = true
  try {
    const data = await UserApi.getInfo()
    Object.assign(user, data)
    profileImage.value = data.profileImageUrl || ''
    setAuthUser(data)
    notifyAuthChanged()
    user.passwordLength = user.provider === 'LOCAL' ? 8 : 0
  } finally { isLoading.user = false }
}
const fetchReservations = async () => {
  isLoading.history = true
  try {
    const base = await getMy(0, 20)
    const names = await Promise.all(
      base.map(r => http.get(`/hotels/${r.hotelId}`).then(res => res.data.hotel.name).catch(() => `호텔 ID: ${r.hotelId}`))
    )
    reservations.value = base.map((r, i) => ({
      ...r,
      hotelName: names[i],
      roomType: r.roomType || '스탠다드',
      statusText: statusMap[r.status] || r.status,
      active: false
    }))
  } catch (e) {
    console.error('예약 내역 조회 실패', e?.response?.status, e?.response?.data || e)
    reservations.value = []
  } finally { isLoading.history = false }
}

onMounted(() => {
  checkAuthStatus()
  if (isLoggedIn.value) {
    fetchUserProfile().then(fetchReservations)
  } else {
    isLoading.user = false
    isLoading.history = false
  }
})
</script>

<style scoped src="@/assets/css/mypage/myaccount.css"></style>
<style scoped src="@/assets/css/mypage/myhistory.css"></style>
