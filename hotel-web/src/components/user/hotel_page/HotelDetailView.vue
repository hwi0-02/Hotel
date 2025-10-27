<!-- src/components/user/hotel_page/HotelDetail.vue -->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import HotelApi from '@/api/HotelApi'
import http, { resolveBackendUrl } from '@/api/http'
import ReservationApi from '@/api/ReservationApi'
import DetailSearchBar from './DetailSearchBar.vue'
import { getAuthUser } from '@/utils/auth-storage'

const route = useRoute()
const router = useRouter()

const isLoggedIn = () => !!getAuthUser()
const currentFullPath = () => router.currentRoute.value.fullPath
const redirectToLogin = () => router.push({ path: '/login', query: { redirect: currentFullPath() } })

const isLoading = ref(true)
const loadError = ref(null)
const hotel = ref(null)
const rooms = ref([])
const reserving = ref(false)

/* =========================
 * 평점/라벨/색상 유틸
 * ========================= */
const rating = ref({ score: 0, subs: { 리뷰수: 0 }, details: fillDetailDefaults() })

const displayScore = computed(() => {
  const s = Number(rating.value.score || 0)
  return Math.round(s)
})

const ratingLabel = computed(() => {
  const avg = Number(displayScore.value || 0)
  if (avg >= 4.5) return '최고'
  if (avg >= 4.0) return '우수'
  if (avg >= 3.5) return '좋음'
  if (avg >= 3.0) return '보통'
  return '나쁨'
})

function getColor(score) {
  if (score >= 4.5) return '#007A33'
  if (score >= 4.0) return '#00A36C'
  if (score >= 3.5) return '#0071c2'
  if (score >= 3.0) return '#ffb400'
  return '#d9534f'
}

function translateLabel(key) {
  const map = {
    cleanliness: '숙소 청결 상태',
    service: '서비스',
    value: '가격 대비 만족도',
    location: '위치',
    facilities: '부대시설',
  }
  return map[key] || key
}

/* =========================
 * 세부항목 정렬/정규화
 * ========================= */
const DETAIL_ORDER = ['service', 'facilities', 'value', 'cleanliness', 'location']

// 한글 키 → 영문 키 매핑
const KEY_MAP = {
  '숙소 청결 상태': 'cleanliness',
  '서비스': 'service',
  '가격 대비 만족도': 'value',
  '위치': 'location',
  '부대시설': 'facilities',
}
function normalizeDetailKeys(det = {}) {
  const out = {}
  for (const [k, v] of Object.entries(det)) {
    const key = KEY_MAP[k] || k
    out[key] = Number(v) || 0
  }
  return out
}
function fillDetailDefaults(det = {}) {
  const base = {
    cleanliness: 0,
    service: 0,
    value: 0,
    location: 0,
    facilities: 0,
  }
  for (const key of Object.keys(base)) {
    const num = Number(det[key])
    base[key] = Number.isFinite(num) ? num : 0
  }
  return base
}
const sortDetailEntries = (obj = {}) => {
  const arr = Object.entries(obj).map(([k, v]) => [k, Number(v) || 0])
  return arr.sort((a, b) => {
    const ia = DETAIL_ORDER.indexOf(a[0]); const ib = DETAIL_ORDER.indexOf(b[0])
    if (ia !== -1 || ib !== -1) {
      if (ia === -1) return 1
      if (ib === -1) return -1
      if (ia !== ib) return ia - ib
    }
    return b[1] - a[1]
  })
}
const detailsForView = computed(() => sortDetailEntries(normalizeDetailKeys(rating.value?.details || {})))

const ratingStorageKey = (hid) => `hotelRating_${hid}`

const canUseCookies = () => typeof document !== 'undefined'

function setCookie(name, value, days = 1) {
  if (!canUseCookies()) return
  const expires = (() => {
    if (!Number.isFinite(days)) return ''
    const date = new Date()
    date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000)
    return `; expires=${date.toUTCString()}`
  })()
  document.cookie = `${name}=${encodeURIComponent(value)}${expires}; path=/; SameSite=Lax`
}

function getCookie(name) {
  if (!canUseCookies()) return null
  const cookies = document.cookie ? document.cookie.split('; ') : []
  for (const entry of cookies) {
    const [key, ...rest] = entry.split('=')
    if (key === name) {
      return decodeURIComponent(rest.join('='))
    }
  }
  return null
}

const hasRatingData = () => {
  const score = Number(rating.value?.score || 0)
  const count = Number(rating.value?.subs?.리뷰수 || 0)
  return score > 0 || count > 0
}

const sanitizeRatingPayload = (raw) => {
  if (!raw || typeof raw !== 'object') return null

  const subs = raw.subs || raw.summary || {}
  const countCandidate = subs['리뷰수'] ?? subs.reviewCount ?? subs.count ?? raw.count
  const count = Number(countCandidate)
  const score = Number(raw.score ?? raw.average ?? raw.avg ?? 0)

  const normalizedDetails = normalizeDetailKeys(raw.details || {})
  const details = fillDetailDefaults(normalizedDetails)

  return {
    score: Number.isFinite(score) ? Math.round(score * 10) / 10 : 0,
    subs: { 리뷰수: Number.isFinite(count) ? count : 0 },
    details
  }
}

const cacheRatingPayload = (hid, payload) => {
  if (!hid || !payload) return
  try {
    const serialized = JSON.stringify({
      average: payload.score ?? 0,
      count: payload.subs?.리뷰수 ?? 0,
      details: payload.details ?? {}
    })
    setCookie(ratingStorageKey(hid), serialized, 3)
  } catch (_) {
    /* ignore cookie errors */
  }
}

const applyRatingPayload = (raw, { cache = false, force = false } = {}) => {
  const sanitized = sanitizeRatingPayload(raw)
  if (!sanitized) return false

  if (!force && hasRatingData()) {
    const incomingCount = Number(sanitized.subs?.리뷰수 ?? 0)
    const incomingScore = Number(sanitized.score ?? 0)
    if (incomingCount === 0 && incomingScore === 0) {
      return false
    }
  }

  rating.value = sanitized
  hotel.value = hotel.value ? { ...hotel.value, rating: sanitized } : { rating: sanitized }

  if (cache) {
    cacheRatingPayload(route.params.id, sanitized)
  }
  return true
}

/* =========================
 * 기타 상태/유틸
 * ========================= */
const wished   = ref(false)
const wishBusy = ref(false)

const checkInStr  = computed(() => route.query.checkIn || null)
const checkOutStr = computed(() => route.query.checkOut || null)
const adultsUrl   = computed(() => route.query.adults   ? Number(route.query.adults)   : 0)
const childrenUrl = computed(() => route.query.children ? Number(route.query.children) : 0)

const money = (n) => Number.isFinite(n) ? '₩ ' + Number(n).toLocaleString('ko-KR') : '요금 문의'
const fmtTime = (t) => (typeof t === 'string' ? t.slice(0,5) : null)

const gallery  = computed(() => hotel.value?.images ?? [])
const badges   = computed(() => hotel.value?.badges ?? [])

const sizeText = (s) => {
  if (s == null) return '-'
  if (typeof s === 'number' && Number.isFinite(s)) return `${s}㎡`
  const n = Number(String(s).replace(/[^\d]/g, ''))
  return Number.isFinite(n) && n > 0 ? `${n}㎡` : '-'
}

const HIGHLIGHT_DEF = {
  city_center:     { ic:'📍', title:'도심 근접',       sub:'중심가 위치' },
  activity:        { ic:'⭐', title:'다양한 액티비티',   sub:'투어/이벤트' },
  airport_shuttle: { ic:'🚌', title:'공항 이동 교통편', sub:'셔틀/픽업' },
  checkin_24h:     { ic:'🕒', title:'24시간 체크인',    sub:'야간 도착 OK' },
}
const highlights = computed(() => {
  const direct = hotel.value?.highlights
  if (Array.isArray(direct) && direct.length) return direct
  const keys = hotel.value?.highlightKeys
  if (Array.isArray(keys) && keys.length) return keys.map(k => HIGHLIGHT_DEF[k]).filter(Boolean)
  return []
})

const amenitiesLeft  = computed(() => hotel.value?.amenities?.left  || [])
const amenitiesRight = computed(() => hotel.value?.amenities?.right || [])

const mapSrc = computed(() => {
  const lat = Number(hotel.value?.lat)
  const lng = Number(hotel.value?.lng)
  if (!Number.isNaN(lat) && !Number.isNaN(lng) && hotel.value?.lat != null && hotel.value?.lng != null) {
    return `https://www.google.com/maps?q=${lat},${lng}&hl=ko&z=15&output=embed`
  }
  const q = hotel.value?.address?.trim() || hotel.value?.name?.trim()
  return q ? `https://www.google.com/maps?q=${encodeURIComponent(q)}&hl=ko&z=15&output=embed` : ''
})

const toBool = (v) => v === true || v === 1 || v === '1' || v === 'true' || v === 'TRUE'

/* =========================
 * 초기 로딩
 * ========================= */
const toAbs = (u) => {
  if (!u) return ''
  if (/^https?:\/\//i.test(String(u))) return String(u)
  const normalized = String(u).startsWith('/') ? String(u) : `/${u}`
  return resolveBackendUrl(normalized)
}

onMounted(async () => {
  try {
    const hid = route.params.id
    const data = await HotelApi.getDetail(hid)
    const h = data?.hotel ?? data ?? null
    const images = Array.isArray(h?.images) ? h.images.map(toAbs) : []
    const cover = toAbs(h?.coverImage || h?.cover || images[0] || '')
    hotel.value = {
      ...h,
      images,
      coverImage: cover || null
    }
    applyRatingPayload(h?.rating, { cache: true })

    const ciYmd = checkInStr.value || null
    const coYmd = checkOutStr.value || null

    const roomsArray = data?.rooms ?? data?.roomList ?? h?.rooms ?? []
    const roomIds = roomsArray.map(r => r?.id).filter(Boolean)

    let reservedRooms = {}
    if (ciYmd && coYmd && roomIds.length > 0) {
      const res = await http.get('reservations/findoverlap', {
        params: { checkIn: ciYmd, checkOut: coYmd, roomIds }
      })
      reservedRooms = res?.data ?? {}
    }

    rooms.value = (Array.isArray(roomsArray) ? roomsArray : []).map(r => {
      const reservedQty = reservedRooms[r.id] ?? 0
      const stock = (r.qty ?? r.room_count ?? r.roomCount ?? 0)
      const remain = Math.max(0, stock - reservedQty)
      const capMax = r.capacity_max ?? r.capacityMax ?? r.capacity ?? 0
      const people = (adultsUrl.value + childrenUrl.value)
      const minRooms = capMax > 0 ? Math.ceil(people / capMax) : 1
      const sizeParsed = (() => {
        const raw = r.size ?? r.room_size ?? r.roomSize ?? null
        if (raw == null) return null
        const n = typeof raw === 'number' ? raw : Number(String(raw).replace(/[^0-9]/g, ''))
        return Number.isFinite(n) ? n : null
      })()
      const photosRaw = Array.isArray(r.photos) ? r.photos
        : Array.isArray(r.images) ? r.images
        : []
      const photos = photosRaw.map(toAbs)

      return {
        id: r.id,
        name: r.name,
        size: sizeParsed,
        capacityMin: r.capacity_min ?? r.capacityMin ?? null,
        capacityMax: capMax || null,
        checkInTime:  r.check_in_time  ?? r.checkInTime  ?? null,
        checkOutTime: r.check_out_time ?? r.checkOutTime ?? null,
        aircon:      toBool(r.aircon),
        bath:        r.bath ?? null,
        bed:         r.bed ?? '',
        cancelPolicy:r.cancel_policy ?? r.cancelPolicy ?? '',
        freeWater:   toBool(r.free_water),
        hasWindow:   toBool(r.has_window),
        originalPrice: r.original_price ?? r.originalPrice ?? null,
        payment:     r.payment ?? '',
        price:       r.price ?? null,
        sharedBath:  toBool(r.shared_bath),
        smoke:       toBool(r.smoke),
        view:        r.view_name ?? r.viewName ?? '',
        wifi:        toBool(r.wifi),
        status:      r.status ?? '',
        roomType:    r.room_type ?? r.roomType ?? '',
        photos,
        qty:         remain,
        roomCount:   Math.max(1, minRooms)
      }
    })

    await loadWishState()

    // ✅ 캐시된 평점 즉시 반영 (정규화 포함)
    const cached = getCookie(ratingStorageKey(hid))
    if (cached && !hasRatingData()) {
      try {
        const parsed = JSON.parse(cached)
        applyRatingPayload({
          score: parsed.average,
          subs: { 리뷰수: parsed.count },
          details: parsed.details
        })
      } catch (_) {}
    }

    // ✅ 서버에서도 새로 불러오기
    await refreshHotelRating()

  } catch (e) {
    console.error(e)
    loadError.value = '숙소 정보를 불러오지 못했어요.'
  } finally {
    isLoading.value = false
  }
})

/* YYYY-MM-DD 가드 */
function ymdGuard(s) {
  return typeof s === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(s) ? s : null
}

/* 예약 */
async function reserve(room) {
  if (!room?.id) { alert('객실 정보 오류'); return }
  const ci = ymdGuard(checkInStr.value)
  const co = ymdGuard(checkOutStr.value)
  if (!ci || !co) { alert('상단 바에서 체크인/아웃을 먼저 선택해주세요.'); return }

  if (!isLoggedIn()) {
    alert('예약은 로그인 후 이용할 수 있어요.')
    return redirectToLogin()
  }

  const qty = Number(room.roomCount || 1)
  if (!Number.isFinite(qty) || qty < 1) { alert('수량을 1 이상으로 입력해주세요.'); return }
  if (room.qty != null && qty > room.qty) { alert(`남은 객실은 최대 ${room.qty}개입니다.`); return }

  reserving.value = true
  try {
    const payload = {
      userId: 1, // TODO: 실제 로그인 유저 ID로 변경
      roomId: room.id,
      qty,
      checkIn: ci,
      checkOut: co,
      adults: adultsUrl.value || 1,
      children: childrenUrl.value || 0,
  holdSeconds: 600
    }
    const res = await ReservationApi.hold(payload)

    // ✅ [핵심] 호텔별 예약 ID 저장
    const hid = route.params.id
    localStorage.setItem(`reservationId_hotel_${hid}`, res.reservationId)

    // ✅ 이후 예약 페이지 이동
    router.push({
      name: 'ReservationCheckout',
      params: { id: res.reservationId },
      query: { hotelId: hid }
    })
  } catch (e) {
    const status = e?.response?.status
    if (status === 401 || status === 403) {
      alert('로그인이 필요합니다.')
      return redirectToLogin()
    }
    alert(e?.response?.data?.message || '홀드 실패')
    console.error(e)
  } finally {
    reserving.value = false
  }
}

/* ==============================
 * 🔖 위시리스트(찜) 클라이언트
 * ============================== */
async function loadWishState() {
  if (!isLoggedIn()) { wished.value = false; return }
  try {
    const { data } = await http.get('wishlists')
    const hid = String(route.params.id)
    wished.value = Array.isArray(data) && data.some(w => String(w.hotelId) === hid)
  } catch (_) {
    wished.value = false
  }
}

async function toggleWish() {
  if (!isLoggedIn()) {
    alert('찜은 로그인 후 이용할 수 있어요.')
    return redirectToLogin()
  }
  if (!hotel.value?.id && !route.params.id) return

  wishBusy.value = true
  try {
    const hid = Number(route.params.id)
    if (wished.value) {
      await http.delete(`wishlists/${hid}`)
      wished.value = false
    } else {
      await http.post('wishlists', { hotelId: hid })
      wished.value = true
    }
  } catch (e) {
    alert(e?.response?.data?.message || '처리에 실패했습니다.')
  } finally {
    wishBusy.value = false
  }
}

/* ✅ 호텔 평점 새로고침 (리뷰 작성/수정 후 호출) */
async function refreshHotelRating() {
  try {
    const hid = route.params.id
    const { data } = await http.get(`reviews/hotels/${hid}/stats`)
    if (data && typeof data.average !== 'undefined') {
      applyRatingPayload(
        { score: data.average, subs: { 리뷰수: data.count }, details: data.details },
        { cache: true, force: true }
      )
    }
  } catch (e) {
    console.error('평점 갱신 실패', e)
  }
}

// ✅ 리뷰 페이지 이동
function goToReviews() {
  const hid = route.params.id
  const reservationId = localStorage.getItem(`reservationId_hotel_${hid}`)
  router.push({
    name: 'HotelReviewView',
    params: { id: hid },
    query: {
      reservationId: reservationId || '',
      hotelName: hotel.value?.name || '',
      hotelImage: hotel.value?.images?.[0] || '',
      redirect: router.currentRoute.value.fullPath
    }
  })
}
</script>

<template>
  <section class="hotel-detail container">
    <div v-if="isLoading" class="loading">불러오는 중…</div>
    <div v-else-if="loadError" class="error">{{ loadError }}</div>

    <template v-else>
      <DetailSearchBar :top="72" />

      <div class="gallery" aria-label="숙소 이미지 갤러리">
        <img
          v-if="gallery[0]" class="hero" :src="gallery[0]" alt="대표 이미지"
          loading="lazy" decoding="async"
          @error="e=> e.target.src='https://picsum.photos/seed/fallback/1200/720'"
        />
        <img
          v-if="gallery[1]" class="thumb" :src="gallery[1]" alt="보조 이미지 1"
          loading="lazy" decoding="async"
          @error="e=> e.target.src='https://picsum.photos/seed/fallback1/600/360'"
        />
        <img
          v-if="gallery[2]" class="thumb" :src="gallery[2]" alt="보조 이미지 2"
          loading="lazy" decoding="async"
          @error="e=> e.target.src='https://picsum.photos/seed/fallback2/600/360'"
        />
      </div>

      <div class="titlebox">
        <div class="title-meta">
          <h1>{{ hotel?.name || '숙소명' }}</h1>
          <p class="addr">{{ hotel?.address }}</p>
        </div>

        <!-- 💗 찜 버튼 -->
        <button
          class="wish-btn"
          :class="{ on: wished }"
          :disabled="wishBusy"
          @click="toggleWish"
          :aria-pressed="wished"
          :title="wished ? '찜 취소' : '찜하기'"
        >
          <span class="heart" aria-hidden="true">♥</span>
          <span class="txt">{{ wished ? '찜함' : '찜하기' }}</span>
        </button>
      </div>

      <section class="overview-grid">
        <div class="ov-left">
          <div class="badges" v-if="badges.length">
            <span v-for="b in badges" :key="b" class="badge">{{ b }}</span>
          </div>

          <div class="highlights" v-if="highlights.length">
            <div v-for="h in highlights" :key="h.title + h.sub" class="hi">
              <div class="hi-ic">{{ h.ic }}</div>
              <div class="hi-txt">
                <div class="hi-title">{{ h.title }}</div>
                <div class="hi-sub">{{ h.sub }}</div>
              </div>
            </div>
          </div>

          <div class="panel" v-if="amenitiesLeft.length || amenitiesRight.length">
            <div class="panel-title">편의 시설/서비스</div>
            <div class="amenities">
              <div class="col">
                <div v-for="a in amenitiesLeft" :key="'L'+a" class="amen">✔ {{ a }}</div>
              </div>
              <div class="col">
                <div v-for="a in amenitiesRight" :key="'R'+a" class="amen">✔ {{ a }}</div>
              </div>
            </div>
          </div>

          <div class="panel" v-if="hotel?.description">
            <div class="panel-title">숙소 소개</div>
            <p class="desc">
              {{ hotel?.description }}
            </p>
          </div>

          <div class="notice" role="note" aria-live="polite" v-if="hotel?.notice">
            <strong>인기 많은 숙소입니다!</strong>
            <span>{{ hotel.notice }}</span>
          </div>
        </div>

        <aside class="ov-right">
          <div class="rating-card" aria-label="이용후기 요약">
            <div class="score-box">
              <div class="score-main">
                <strong>{{ Number(displayScore).toFixed(1) }}</strong>
                <span class="label">{{ ratingLabel }}</span>
              </div>
              <p class="count">이용후기 {{ rating.subs?.리뷰수 || 0 }}건 기준</p>
            </div>

            <div v-if="detailsForView.length" class="score-details">
              <div
                class="detail-item"
                v-for="item in detailsForView"
                :key="item[0]"
              >
                <span class="name">{{ translateLabel(item[0]) }}</span>
                <div class="bar">
                  <div
                    class="fill"
                    :class="{
                      low: Number(item[1]) < 3.0,
                      medium: Number(item[1]) >= 3.0 && Number(item[1]) < 3.5,
                      good: Number(item[1]) >= 3.5 && Number(item[1]) < 4.0,
                      verygood: Number(item[1]) >= 4.0 && Number(item[1]) < 4.5,
                      excellent: Number(item[1]) >= 4.5
                    }"
                    :style="{ width: ((Number(item[1]) || 0) / 5) * 100 + '%' }"
                  ></div>
                </div>
                <span class="val">{{ Number(item[1] ?? 0).toFixed(1) }}</span>
              </div>
            </div>

            <a href="#" class="link" @click.prevent="goToReviews">이용후기 모두 보기</a>
          </div>

          <div class="map-card" v-if="mapSrc">
            <div class="map-embed">
              <iframe
                :src="mapSrc"
                style="border:0"
                loading="lazy"
                referrerpolicy="no-referrer-when-downgrade"
                allowfullscreen
              ></iframe>
            </div>
          </div>
        </aside>
      </section>

      <section class="rooms">
        <h2>객실을 선택하세요</h2>

        <article v-for="room in rooms" :key="room.id" class="room">
          <div class="r-media">
            <div class="photos">
              <img
                :src="room.photos?.[0]" :alt="room.name" loading="lazy" decoding="async"
                @error="e=> e.target.src='https://picsum.photos/seed/room_fallback/480/320'"
              />
              <div class="thumbs">
                <img
                  v-for="(p,i) in (room.photos || []).slice(1,4)"
                  :key="i" :src="p" :alt="room.name + ' 썸네일 ' + (i+1)"
                  loading="lazy" decoding="async"
                  @error="e=> e.target.src='https://picsum.photos/seed/room_thumb/140/100'"
                />
              </div>
            </div>
          </div>

          <div class="r-benefits">
            <div class="r-title"><h3>{{ room.name }}</h3></div>

            <div class="r-details">
              <div class="detail"><span class="label">객실 크기</span><span class="value">{{ sizeText(room.size) }}</span></div>
              <div class="detail" v-if="room.view"><span class="label">전망</span><span class="value">{{ room.view }}</span></div>
              <div class="detail"><span class="label">침대</span><span class="value">{{ room.bed || '-' }}</span></div>
              <div class="detail" v-if="room.bath != null"><span class="label">욕실</span><span class="value">{{ room.bath }}개</span></div>
              <div class="detail"><span class="label">흡연</span><span class="value">{{ room.smoke ? '가능' : '금연' }}</span></div>
              <div class="detail"><span class="label">창문</span><span class="value">{{ room.hasWindow ? '있음' : '없음' }}</span></div>
              <div class="detail"><span class="label">에어컨</span><span class="value">{{ room.aircon ? '있음' : '없음' }}</span></div>
              <div class="detail"><span class="label">무료 생수</span><span class="value">{{ room.freeWater ? '제공' : '미제공' }}</span></div>
              <div class="detail"><span class="label">Wi-Fi</span><span class="value">{{ room.wifi ? '무료' : '없음' }}</span></div>
              <div class="detail" v-if="room.capacityMin != null || room.capacityMax != null">
                <span class="label">정원</span><span class="value">{{ room.capacityMin ?? '?' }}–{{ room.capacityMax ?? '?' }}명</span>
              </div>
              <div class="detail" v-if="room.checkInTime || room.checkOutTime">
                <span class="label">체크인/아웃</span><span class="value">{{ fmtTime(room.checkInTime) }} / {{ fmtTime(room.checkOutTime) }}</span>
              </div>
              <div class="detail" v-if="room.roomType"><span class="label">타입</span><span class="value">{{ room.roomType }}</span></div>
              <div class="detail" v-if="room.status"><span class="label">상태</span><span class="value">{{ room.status }}</span></div>
              <div class="detail"><span class="label">남은 객실</span><span class="value">{{ room.qty }}개</span></div>
              <div class="detail" v-if="room.cancelPolicy"><span class="label">취소 정책</span><span class="value">{{ room.cancelPolicy }}</span></div>
              <div class="detail" v-if="room.payment"><span class="label">결제 방식</span><span class="value">{{ room.payment }}</span></div>
            </div>
          </div>

          <div class="r-cta">
            <div class="price">
              <div class="orig" v-if="room.originalPrice">{{ money(room.originalPrice) }}</div>
              <div class="now">{{ money(room.price) }}</div>
              <div class="tax">1박당 요금(세금/봉사료 제외)</div>
            </div>

            <div class="qty">
              <input
                type="number"
                v-model.number="room.roomCount"
                :min="Math.max(1, Math.ceil((adultsUrl + childrenUrl) / (room.capacityMax || 1)))"
                :max="room.qty"
                @change="room.roomCount = Math.min(Math.max(1, Number(room.roomCount)), room.qty ?? Number.MAX_SAFE_INTEGER)"
                aria-label="수량"
              />
              <div class="left" v-if="room.qty != null">남은 {{ room.qty }}개</div>
            </div>

            <button
              class="btn primary"
              :disabled="reserving || room.qty === 0"
              @click="reserve(room)"
              :title="room.qty === 0 ? '품절' : '지금 예약하기'"
            >
              {{ room.qty === 0 ? '품절' : (reserving ? '처리 중…' : '지금 예약하기') }}
            </button>
          </div>
        </article>
      </section>
    </template>
  </section>
</template>

<style src="@/assets/css/hotel_detail/hotel_detail.css"></style>
