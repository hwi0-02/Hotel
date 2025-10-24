<template>
  <div class="reservation-result-layout">
    <main class="result-container">
      <h1 class="page-title">예약이 완료되었습니다 🎉</h1>

      <div v-if="isLoading" class="status-message">예약 정보를 불러오는 중...</div>
      <div v-else-if="error" class="status-message error">{{ error }}</div>

      <div v-else-if="reservation && hotel" class="result-grid">
        <!-- 👈 왼쪽: 예약 요약 + 결제 요약 -->
        <section class="card">
          <header class="section-head">
            <div>
              <h2 class="section-title">예약 요약</h2>
              <div class="subtitle">
                <span class="status-badge" :class="reservation.status">{{ displayReservation.statusText }}</span>
              </div>
            </div>
          </header>

          <!-- 예약 정보 -->
          <div class="detail-group">
            <div class="detail-item">
              <span class="detail-label">예약 번호</span>
              <span class="detail-value">{{ displayReservation.id }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">예약 시간</span>
              <span class="detail-value">{{ displayReservation.bookingTime }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">체크-인</span>
              <span class="detail-value">{{ displayReservation.checkInDate }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">체크-아웃</span>
              <span class="detail-value">{{ displayReservation.checkOutDate }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">투숙 인원</span>
              <span class="detail-value">
                성인 {{ displayReservation.adults ?? 0 }}명
                <template v-if="displayReservation.children > 0">, 아동 {{ displayReservation.children }}명</template>
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">객실 수</span>
              <span class="detail-value">{{ displayReservation.numRooms ?? 0 }}개</span>
            </div>
          </div>

          <!-- 결제 요약 -->
          <div class="split-title">결제 내역</div>
          <div class="pay-grid">
            <div class="row">
              <span class="label">숙박 박수</span>
              <span class="value">{{ nights }}박</span>
            </div>
            <div class="row">
              <span class="label">객실 수</span>
              <span class="value">{{ rooms }}개</span>
            </div>
            <div class="row">
              <span class="label">1박 추정가 (객실 1개)</span>
              <span class="value">{{ unitPrice == null ? '-' : currency(unitPrice) }}</span>
            </div>
            <div class="row total">
              <span class="label">총 결제 금액</span>
              <span class="value strong">{{ currency(totalPrice) }}</span>
            </div>
          </div>

          <!-- 액션 버튼 -->
          <div class="button-group">
            <router-link class="btn primary" to="/search">다른 숙소 보기</router-link>
            <button class="btn" @click="goHome">홈으로</button>
            <button class="btn outline" @click="printReceipt">영수증 인쇄</button>
          </div>
        </section>

        <!-- 👉 오른쪽: 호텔 카드 (작은 썸네일) -->
        <aside class="hotel-card card">
          <div class="hotel-head">
            <img :src="displayHotel.image" :alt="displayHotel.name" class="hotel-thumb" @error="onImgErr" />
            <div class="hotel-meta">
              <h3 class="hotel-name">{{ displayHotel.name }}</h3>
              <p class="hotel-addr">{{ hotel?.address || '주소 정보 없음' }}</p>
            </div>
          </div>
          <p class="hotel-desc" v-if="displayHotel.description">{{ displayHotel.description }}</p>
        </aside>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ReservationApi from '@/api/ReservationApi'
import http from '@/api/http'

const route = useRoute()
const router = useRouter()

// --- 상태 ---
const isLoading = ref(true)
const error = ref(null)
const reservation = ref(null)
const hotel = ref(null)
const room = ref(null)
const paymentAmount = ref(null) // 결제 레코드 amount(표시용 SoT)


// --- 데이터 로딩 ---
const loadReservationDetails = async () => {
  isLoading.value = true
  error.value = null
  try {
    const reservationId = route.params.id
    if (!reservationId) throw new Error("예약 ID가 없습니다.")

    const resData = await ReservationApi.get(reservationId)
    const hotelRes = await http.get(`/hotels/${resData.hotelId}`)

    // 객실 정보는 선택(있으면)
    try {
      const roomRes = await http.get(`/rooms/${resData.roomId}`)
      room.value = roomRes.data?.room ?? roomRes.data ?? null
    } catch { room.value = null }

    reservation.value = resData
    hotel.value = hotelRes.data?.hotel ?? hotelRes.data ?? {}
  } catch (e) {
    console.error("예약 정보를 불러오는 데 실패:", e)
    error.value = "예약 정보를 불러올 수 없습니다."
  } finally {
    isLoading.value = false
  }
}

// 결제 레코드 금액 조회 (표시용)
const loadPaymentAmountSafely = async () => {
  try {
    const pid = sessionStorage.getItem('lastPaymentId')
    if (pid) {
      const r2 = await http.get(`/payments/${pid}`)
      const a2 = Number(r2?.data?.amount)
      if (Number.isFinite(a2)) paymentAmount.value = a2
    }
  } catch (e) {
    console.warn('표시용 결제 금액 조회 실패:', e)
  }
}

onMounted(async () => {
  await loadReservationDetails()
  await loadPaymentAmountSafely()
})

// --- 포맷/도우미 ---
const statusMap = {
  CONFIRMED: "예약 확정",
  COMPLETED: "이용 완료",
  CANCELLED: "취소됨",
  PENDING: "결제 대기중",
  HOLD: "임시 예약"
}

const formatDate = (iso) => {
  if (!iso) return ''
  return new Date(iso).toLocaleDateString('ko-KR', { year:'numeric', month:'long', day:'numeric' })
}
const formatDateTime = (iso) => {
  if (!iso) return ''
  const d = new Date(iso)
  const dateStr = d.toLocaleDateString('ko-KR', { year:'numeric', month:'long', day:'numeric' })
  const timeStr = d.toLocaleTimeString('ko-KR', { hour:'2-digit', minute:'2-digit', second:'2-digit', hour12:false })
  return `${dateStr} ${timeStr}`
}
const currency = (n) => `₩ ${Number(n || 0).toLocaleString('ko-KR')}`
const onImgErr = (e) => { e.target.src = 'https://placehold.co/160x120/e9eef5/7a869a?text=Hotel' }

// 박수 계산(UTC 기준)
function toYmd(s){
  if (!s) return ''
  const t = String(s)
  return t.includes('T') ? t.split('T')[0] : t
}
function diffNights(startYmd, endYmd){
  if (!startYmd || !endYmd) return 0
  const [sy, sm, sd] = startYmd.split('-').map(Number)
  const [ey, em, ed] = endYmd.split('-').map(Number)
  const utcA = Date.UTC(sy, sm - 1, sd)
  const utcB = Date.UTC(ey, em - 1, ed)
  return Math.max(1, Math.round((utcB - utcA) / 86400000))
}

// 표시 데이터
const displayHotel = computed(() => {
  if (!hotel.value) return {}
  return {
    ...hotel.value,
    image: hotel.value.images?.[0] || 'https://placehold.co/320x220/e9eef5/7a869a?text=Hotel',
    description: hotel.value.description
  }
})
const displayReservation = computed(() => {
  if (!reservation.value) return {}
  return {
    ...reservation.value,
    id: `R-${String(reservation.value.id).padStart(6, '0')}`,
    checkInDate: formatDate(reservation.value.startDate),
    checkOutDate: formatDate(reservation.value.endDate),
    totalPrice: paymentAmount.value ?? reservation.value.totalPrice ?? 0,
    statusText: statusMap[reservation.value.status] || reservation.value.status,
    adults: reservation.value.adults,
    children: reservation.value.children,
    numRooms: reservation.value.numRooms,
    bookingTime: formatDateTime(reservation.value.createdAt),
  }
})

// 결제 breakdown
const nights = computed(() => {
  if (!reservation.value) return 0
  return diffNights(toYmd(reservation.value.startDate), toYmd(reservation.value.endDate))
})
const rooms = computed(() => Number(displayReservation.value.numRooms || 1))
const totalPrice = computed(() => Number(displayReservation.value.totalPrice || 0))
const unitPrice = computed(() => {
  const denom = nights.value * rooms.value
  if (!denom) return null
  const val = totalPrice.value / denom
  return Number.isFinite(val) ? Math.round(val) : null
})

// 프린트
const printReceipt = () => window.print()
</script>

<style scoped>
/* ===========================
   HotelDetail와 같은 톤
   =========================== */
.reservation-result-layout{
  --ink:#1f2937;
  --ink-2:#4b5563;
  --ink-3:#6b7280;
  --line:#e5e7eb;
  --accent:#39c5a0;
  --card-bg:#ffffff;
  --danger:#b00020;

  color:var(--ink);
  background:#fff;
  -webkit-font-smoothing:antialiased;
  -moz-osx-font-smoothing:grayscale;
}

.result-container{
  max-width:1120px;
  margin:0 auto;
  padding:18px 16px 40px;
}

.page-title{
  font-size:28px; font-weight:900; margin:8px 0 18px; letter-spacing:-.2px;
}

/* ===== 레이아웃 ===== */
.result-grid{
  display:grid; grid-template-columns: 2fr 1.1fr; gap:18px;
}
@media (max-width: 980px){
  .result-grid{ grid-template-columns:1fr; }
}

/* ===== 카드 공통 ===== */
.card{
  background:var(--card-bg);
  border:1px solid var(--line);
  border-radius:16px;
  padding:16px;
  box-shadow:0 6px 18px rgba(0,0,0,.04);
}

/* 섹션 헤더 */
.section-head{ display:flex; align-items:flex-end; justify-content:space-between; margin-bottom:8px; }
.section-title{ font-size:20px; font-weight:900; margin:0; }
.subtitle{ color:var(--ink-3); }

/* 상태 메시지 */
.status-message{ padding:16px 12px; color:var(--ink-2); }
.status-message.error{ color:var(--danger); }

/* 예약 상세 리스트 */
.detail-group{ display:grid; gap:8px; margin-top:8px; }
.detail-item{
  display:flex; justify-content:space-between; gap:12px;
  border-bottom:1px dashed #eef2f7; padding:8px 0;
}
.detail-label{ color:var(--ink-3); }
.detail-value{ font-weight:800; color:#0f172a; }

/* 결제 요약 */
.split-title{
  margin:14px 0 8px;
  font-size:16px; font-weight:900; color:#334155;
  border-top:1px solid var(--line); padding-top:12px;
}
.pay-grid{ display:grid; gap:10px; }
.pay-grid .row{ display:flex; justify-content:space-between; }
.pay-grid .label{ color:var(--ink-3); }
.pay-grid .value{ font-weight:800; }
.pay-grid .total{ margin-top:4px; padding-top:6px; border-top:1px dashed #e2e8f0; }
.pay-grid .strong{ color:var(--accent); font-size:18px; }

/* 버튼들 */
.button-group{
  margin-top:16px; display:flex; gap:10px; flex-wrap:wrap;
}
.btn{
  height:42px; padding:0 14px; border-radius:12px; border:1px solid var(--line);
  background:#f7f8fa; font-weight:900; cursor:pointer;
  display:inline-flex; align-items:center; justify-content:center;
  transition: box-shadow .15s ease, transform .1s ease, background .15s ease, border-color .15s ease;
  text-decoration:none; color:inherit;
}
.btn:hover{ box-shadow:0 6px 18px rgba(0,0,0,.06); transform: translateY(-1px); }
.btn.primary{ background:var(--accent); border-color:#2bb38f; color:#fff; }
.btn.outline{ background:#fff; border-style:dashed; }

/* 호텔 카드 (오른쪽) */
.hotel-card{ position:sticky; top:72px; }
.hotel-head{ display:flex; gap:12px; align-items:center; }
.hotel-thumb{
  width:112px; height:84px; object-fit:cover; border-radius:12px; border:1px solid var(--line); flex-shrink:0;
}
.hotel-meta{ min-width:0; }
.hotel-name{ margin:0; font-size:18px; font-weight:900; }
.hotel-addr{ margin:4px 0 0; color:var(--ink-3); font-size:13px; }
.hotel-desc{ margin-top:10px; color:var(--ink-2); line-height:1.6; }

/* 상태 뱃지 */
.status-badge{
  display:inline-flex; align-items:center; gap:6px;
  border:1px solid var(--line); border-radius:999px;
  padding:4px 10px; font-weight:800; font-size:12px;
}
.status-badge.CONFIRMED,
.status-badge.COMPLETED{ color:#065f46; background:#eafff7; border-color:#a7f3d0; }
.status-badge.PENDING{ color:#0b57d0; background:#eef6ff; border-color:#bfdbfe; }
.status-badge.CANCELLED{ color:#9ca3af; background:#f3f4f6; border-color:#e5e7eb; }

/* 프린트 최적화 */
@media print{
  .reservation-result-layout Header,
  .button-group{ display:none !important; }
  .result-container{ padding:0; }
  .card{ box-shadow:none; }
}
</style>
