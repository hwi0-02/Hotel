<template>
  <div class="page">
    <header class="page-header">
      <h2>호텔 수정</h2>
      <div class="gap"></div>
      <button class="btn" @click="save" :disabled="saving">저장</button>
    </header>

    <div v-if="loading" class="hint">불러오는 중…</div>
    <div v-else-if="error" class="error">{{ error }}</div>

    <form v-else class="form">
      <!-- 기본 정보 -->
      <div class="row">
        <label>이름</label>
        <input v-model.trim="f.name" type="text" />
      </div>
      <div class="row">
        <label>주소</label>
        <input v-model.trim="f.address" type="text" />
      </div>
      <div class="row">
        <label>등급(1~5)</label>
        <input v-model.number="f.starRating" type="number" min="1" max="5" />
      </div>

      <!-- 승인 상태 -->
      <div class="row">
        <label>승인 상태</label>
        <select v-model="f.approvalStatus">
          <option value="PENDING">PENDING</option>
          <option value="APPROVED">APPROVED</option>
          <option value="REJECTED">REJECTED</option>
          <option value="SUSPENDED">SUSPENDED</option>
        </select>
      </div>

      <!-- 검색 노출 제어 -->
      <div class="row">
        <label>검색 노출</label>
        <label style="display:flex;align-items:center;gap:8px;">
          <input type="checkbox" v-model="f.visibleInSearch" />
          <span>{{ f.visibleInSearch ? '노출함' : '노출 안 함' }}</span>
        </label>
      </div>

      <div class="row">
        <label>소개</label>
        <textarea v-model.trim="f.description" rows="4"/>
      </div>

      <!-- 편의시설: 단일 소스 -->
      <fieldset class="panel">
        <legend>편의시설</legend>

        <div class="amen-grid">
          <div class="group">
            <div class="col-title">시설</div>
            <label v-for="a in FACILITY" :key="a.key" class="chk">
              <input type="checkbox" :value="a.key" v-model="amenAll" />
              <SafeHtml class="ic" tag="span" :content="a.ic" />
              <span>{{ a.label }}</span>
            </label>
          </div>

          <div class="group">
            <div class="col-title">서비스</div>
            <label v-for="a in SERVICE" :key="a.key" class="chk">
              <input type="checkbox" :value="a.key" v-model="amenAll" />
              <SafeHtml class="ic" tag="span" :content="a.ic" />
              <span>{{ a.label }}</span>
            </label>
          </div>
        </div>

        <div class="empty-box" v-if="amenAll.length === 0">
          등록된 편의시설이 없습니다. 적어도 하나를 선택해 주세요.
        </div>
      </fieldset>

      <!-- 지도: 이름/주소 기반만 -->
      <fieldset class="panel">
        <legend>지도</legend>
        <div class="map-wrap" v-if="mapSrc">
          <iframe
            :src="mapSrc"
            style="border:0"
            loading="lazy"
            referrerpolicy="no-referrer-when-downgrade"
            allowfullscreen
          ></iframe>
        </div>
        <small class="hint">주소(없으면 이름)로 지도 검색합니다.</small>
      </fieldset>

      <!-- 호텔 이미지 업로드/관리 -->
      <fieldset class="panel">
        <legend>호텔 이미지</legend>
        <div class="uploader">
          <label class="upload-box">
            <input type="file" multiple accept="image/*" @change="pickHotelImages" />
            <span>+ 이미지 추가</span>
          </label>
          <div class="img-list" v-if="hotelImages.length">
            <div v-for="im in hotelImages" :key="im.id" class="img-item">
              <img :src="im.url" :alt="`hotel image ${im.id}`" />
              <div class="img-actions">
                <button type="button" class="btn-sm" @click="makeCover(im.id)" :disabled="im.cover">
                  {{ im.cover ? '대표 이미지' : '대표로 설정' }}
                </button>
                <button type="button" class="btn-sm danger" @click="removeHotelImage(im.id)">삭제</button>
              </div>
            </div>
          </div>
          <small class="hint">첫 업로드 시 자동으로 대표 이미지가 지정됩니다.</small>
        </div>
      </fieldset>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import SafeHtml from '@/components/common/SafeHtml.vue'

const route = useRoute()
const router = useRouter()

/* 마스터 편의 정의(키/라벨/아이콘/카테고리) */
const AMEN_DEF = [
  { key:'wifi',      label:'무료 Wi-Fi',           ic:'📶', cat:'facility' },
  { key:'breakfast', label:'조식',                 ic:'🥐', cat:'facility' },
  { key:'fitness',   label:'피트니스',              ic:'🏋️', cat:'facility' },
  { key:'pool',      label:'수영장',                ic:'🏊', cat:'facility' },
  { key:'parking',   label:'주차',                  ic:'🅿️', cat:'facility' },
  { key:'spa',       label:'스파',                  ic:'💆', cat:'facility' },

  { key:'frontdesk24', label:'24시간 프런트 데스크', ic:'🕘', cat:'service' },
  { key:'tour',        label:'투어',                 ic:'🗺️', cat:'service' },
  { key:'airport_shuttle', label:'공항 이동 서비스',  ic:'🚌', cat:'service' },
  { key:'laundry',     label:'세탁',                 ic:'🧺', cat:'service' },
  { key:'luggage',     label:'여행 가방 보관',        ic:'🧳', cat:'service' },
  { key:'taxi',        label:'택시 서비스',           ic:'🚕', cat:'service' },
]

const FACILITY = AMEN_DEF.filter(a => a.cat === 'facility')
const SERVICE  = AMEN_DEF.filter(a => a.cat === 'service')

const loading = ref(true)
const saving  = ref(false)
const error   = ref(null)

const f = ref({
  name: '',
  address: '',
  starRating: 3,
  description: '',
  approvalStatus: 'PENDING',
  visibleInSearch: true,
})

/* ✅ 단일 선택 소스 */
const amenAll  = ref([])          // ['wifi','pool', ...]
const hotelImages = ref([])

/* ====== 파생 ====== */
const hasAnyAmenity = computed(() => amenAll.value.length > 0)
const mapSrc = computed(() => {
  const q = f.value.address?.trim() || f.value.name?.trim()
  return q ? `https://www.google.com/maps?q=${encodeURIComponent(q)}&hl=ko&z=15&output=embed` : ''
})

/* 서버에 저장된 과거(좌/우 분리) -> 단일로 흡수 */
function mergeLeftRightToAll(raw) {
  const left  = Array.isArray(raw?.amenities?.left)  ? raw.amenities.left  : []
  const right = Array.isArray(raw?.amenities?.right) ? raw.amenities.right : []
  const allLabels = Array.from(new Set([...left, ...right]))
  // 라벨을 키로 역매핑
  const toKey = (label) => AMEN_DEF.find(a => a.label === label)?.key || null
  const keys = allLabels.map(toKey).filter(Boolean)
  return Array.from(new Set(keys))
}

/* 단일(키) -> 좌/우 분배(라벨) */
function splitAllToLeftRight(keys) {
  const lab = (k) => AMEN_DEF.find(a => a.key === k)?.label
  // 카테고리 기반 분배: 시설→왼쪽, 서비스→오른쪽
  const left  = keys.filter(k => AMEN_DEF.find(a => a.key === k)?.cat === 'facility').map(lab).filter(Boolean)
  const right = keys.filter(k => AMEN_DEF.find(a => a.key === k)?.cat === 'service').map(lab).filter(Boolean)
  return { left, right }
}

onMounted(async () => {
  try {
    const { data } = await http.get(`/owner/hotels/my-hotels/${route.params.id}`)
    f.value = {
      name: data.name,
      address: data.address,
      starRating: data.starRating ?? 3,
      description: data.description ?? '',
      approvalStatus: data.approvalStatus ?? 'PENDING',
      visibleInSearch: data.visibleInSearch ?? true,
    }
    // 좌/우 → 단일 키 세트로
    amenAll.value = mergeLeftRightToAll(data)

    // 호텔 이미지
    hotelImages.value = (await http.get(`/owner/hotels/my-hotels/${route.params.id}/images`)).data ?? []
  } catch (e) {
    console.error(e)
    error.value = '호텔 정보를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
})

async function save() {
  saving.value = true
  error.value = null
  try {
    if (!hasAnyAmenity.value) {
      const goOn = confirm('편의시설이 비어 있어요. 그래도 저장할까요?')
      if (!goOn) { saving.value = false; return }
    }
    // 단일→좌/우 분배(라벨로 보냄: 기존 백엔드 호환)
    const { left, right } = splitAllToLeftRight(amenAll.value)

    const payload = {
      name: f.value.name,
      address: f.value.address,
      starRating: f.value.starRating,
      description: f.value.description,
      approvalStatus: f.value.approvalStatus,
      visibleInSearch: f.value.visibleInSearch,
      amenities: { left, right },
    }
    await http.put(`/owner/hotels/my-hotels/${route.params.id}`, payload)
    alert('저장되었습니다.')
    router.push({ name: 'OwnerHotelList' })
  } catch (e) {
    console.error(e)
    error.value = e?.response?.data?.message || '저장에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

async function pickHotelImages(e) {
  const files = Array.from(e.target.files || [])
  if (!files.length) return
  const fd = new FormData()
  files.forEach(f => fd.append('images', f))
  const { data } = await http.post(`/owner/hotels/my-hotels/${route.params.id}/images`, fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  hotelImages.value = data
  e.target.value = ''
}

async function removeHotelImage(imageId) {
  const ok = confirm('이 이미지를 삭제할까요?')
  if (!ok) return
  await http.delete(`/owner/hotels/my-hotels/${route.params.id}/images/${imageId}`)
  hotelImages.value = hotelImages.value.filter(i => i.id !== imageId)
}

async function makeCover(imageId) {
  const { data } = await http.put(`/owner/hotels/my-hotels/${route.params.id}/images/${imageId}/cover`)
  hotelImages.value = data
}
</script>

<style scoped>
.page { padding:24px; }
.page-header { display:flex; align-items:center; gap:12px; }
.page-header .gap{ flex:1 }
.btn { background:#0ea5e9; color:#fff; border:none; padding:10px 14px; border-radius:8px; font-weight:700; }
.hint,.error{ color:#666; padding:16px 0; }
.form{ margin-top:16px; display:flex; flex-direction:column; gap:12px; }
.row{ display:flex; gap:12px; align-items:center; }
.row > label{ width:120px; color:#444; }
.row > input, .row > textarea, .row > select{ flex:1; border:1px solid #e5e7eb; border-radius:8px; padding:8px 10px; }

.panel{ border:1px solid #e5e7eb; border-radius:10px; padding:12px; }
/* 편의 그리드(2열) */
.amen-grid{ display:grid; grid-template-columns: 1fr 1fr; gap:12px; }
.group{ border:1px dashed #e5e7eb; border-radius:10px; padding:10px; }
.col-title{ font-weight:700; margin-bottom:6px; color:#555; }
.chk{ display:flex; align-items:center; gap:10px; padding:6px 4px; }
.chk .ic{ width:18px; text-align:center; }

.map-wrap{ margin-top:10px; border-radius:12px; overflow:hidden; }
.map-wrap iframe{ width:100%; height:280px; }
.empty-box{
  background:#fafafa; border:1px dashed #e5e7eb; color:#666;
  padding:14px; border-radius:10px; text-align:center;
}

.uploader { display:flex; flex-direction:column; gap:12px; }
.upload-box{
  border:2px dashed #e5e7eb; border-radius:10px; padding:16px; color:#666;
  display:inline-flex; align-items:center; justify-content:center; cursor:pointer; width:200px; text-align:center;
}
.upload-box input{ display:none; }
.img-list{ display:flex; flex-wrap:wrap; gap:12px; }
.img-item{ width:160px; border:1px solid #eee; border-radius:10px; overflow:hidden; background:#fff; }
.img-item img{ display:block; width:100%; height:110px; object-fit:cover; }
.img-actions{ display:flex; gap:6px; padding:8px; justify-content:space-between; }
.btn-sm{ font-size:12px; padding:6px 8px; border:1px solid #e5e7eb; border-radius:6px; background:#fff; cursor:pointer; }
.btn-sm.danger{ color:#dc2626; }
</style>
