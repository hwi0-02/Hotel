<template>
  <div class="business-apply">
    <!-- 🔻 전역(App.vue)에서 헤더/푸터 렌더 → 이 페이지에서는 제거 -->
    <div class="apply-container">
      <div class="apply-header">
        <div class="header-content">
          <h1>사업자 등록 신청</h1>
          <p class="sub-text">Business Registration</p>
          <p class="description">사업자 정보를 입력하여 사업자 등록을 신청하세요.</p>
        </div>
        <div class="header-actions">
          <button 
            v-if="isLoggedIn" 
            @click="showApplicationStatus" 
            class="status-btn"
          >
            신청 현황 보기
          </button>
        </div>
      </div>

      <form @submit.prevent="submitApplication" class="apply-form">
        <div class="input-group">
          <label for="businessName">사업자명 *</label>
          <input
            id="businessName"
            v-model="form.businessName"
            type="text"
            required
            placeholder="사업자명을 입력하세요"
          />
        </div>

        <div class="input-group">
          <label for="hotelName">호텔명 *</label>
          <input
            id="hotelName"
            v-model="form.hotelName"
            type="text"
            required
            placeholder="호텔명을 입력하세요"
          />
        </div>

        <div class="input-group">
          <label for="businessNumber">사업자 등록번호 *</label>
          <input
            id="businessNumber"
            v-model="form.businessNumber"
            type="text"
            required
            placeholder="000-00-00000 형식으로 입력하세요"
            inputmode="numeric"
            pattern="[0-9]{3}-[0-9]{2}-[0-9]{5}"
            title="사업자 등록번호는 000-00-00000 형식으로 입력하세요"
            maxlength="12"
            @input="onBusinessNumberInput"
            autocomplete="off"
          />
        </div>

        <div class="input-group">
          <label for="address">주소 *</label>
          <input
            id="address"
            v-model="form.address"
            type="text"
            required
            placeholder="사업장 주소를 입력하세요"
          />
        </div>

        <div class="input-group">
          <label for="phone">연락처 *</label>
          <input
            id="phone"
            v-model="form.phone"
            type="tel"
            required
            placeholder="010-0000-0000 형식으로 입력하세요"
          />
        </div>

        <div class="form-actions">
          <button type="button" @click="goBack" class="btn btn-secondary">
            취소
          </button>
          <button type="submit" :disabled="loading || hasExistingApplication === true" class="btn btn-primary">
            {{ loading ? '신청 중...' : '신청하기' }}
          </button>
        </div>
      </form>
    </div>

    <!-- 신청 현황 모달 -->
    <div v-if="showStatusModal" class="modal-overlay" @click="closeStatusModal">
      <div class="status-modal" @click.stop>
        <div class="modal-header">
          <h2>사업자 등록 신청 현황</h2>
          <button @click="closeStatusModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-content">
          <div v-if="applicationStatus">
            <div class="status-card">
              <div class="status-info">
                <div class="status-badge" :class="getStatusClass(applicationStatus.status)">
                  {{ getStatusText(applicationStatus.status) }}
                </div>
                <div v-if="applicationStatus.createdAt" class="status-date">
                  신청일: {{ formatDate(applicationStatus.createdAt) }}
                </div>
              </div>
            </div>
            
            <div class="application-details">
              <h3>신청 정보</h3>
              <div class="detail-row" v-if="applicationStatus.businessName">
                <span class="label">사업자명:</span>
                <span class="value">{{ applicationStatus.businessName }}</span>
              </div>
              <div class="detail-row">
                <span class="label">호텔명:</span>
                <span class="value">{{ applicationStatus.name }}</span>
              </div>
              <div class="detail-row" v-if="applicationStatus.businessId">
                <span class="label">사업자등록번호:</span>
                <span class="value">{{ formatBusinessNumber(applicationStatus.businessId) }}</span>
              </div>
              <div class="detail-row">
                <span class="label">주소:</span>
                <span class="value">{{ applicationStatus.address }}</span>
              </div>
              <div class="detail-row" v-if="applicationStatus.phone">
                <span class="label">연락처:</span>
                <span class="value">{{ applicationStatus.phone }}</span>
              </div>
              
              <div v-if="applicationStatus.status === 'APPROVED' && applicationStatus.approvalDate" class="approval-info">
                <div class="detail-row">
                  <span class="label">승인일:</span>
                  <span class="value">{{ formatDate(applicationStatus.approvalDate) }}</span>
                </div>
              </div>
              
              <div v-if="applicationStatus.status === 'REJECTED' && applicationStatus.rejectionReason" class="rejection-info">
                <div class="detail-row">
                  <span class="label">반려 사유:</span>
                  <span class="value rejection-reason">{{ applicationStatus.rejectionReason }}</span>
                </div>
              </div>
            </div>
            
            <div class="modal-actions">
              <button @click="closeStatusModal" class="btn btn-secondary">닫기</button>
              <button
                v-if="applicationStatus.status === 'PENDING'"
                @click="cancelApplication"
                class="btn btn-danger"
                :disabled="canceling"
              >
                {{ canceling ? '취소 중...' : '신청 취소' }}
              </button>
              <button 
                v-if="applicationStatus.status === 'REJECTED'" 
                @click="closeStatusModal" 
                class="btn btn-primary"
              >
                다시 신청하기
              </button>
            </div>
          </div>
          
          <div v-else-if="hasExistingApplication === false" class="no-application">
            <div class="no-application-content">
              <p>아직 사업자 등록 신청 내역이 없습니다.</p>
              <p class="sub-message">아래 폼을 작성하여 사업자 등록을 신청해주세요.</p>
            </div>
            <div class="modal-actions">
              <button @click="closeStatusModal" class="btn btn-primary">신청하러 가기</button>
            </div>
          </div>
          
          <div v-else class="loading-status">
            <p>신청 현황을 불러오는 중...</p>
            <p class="debug-info" style="font-size: 12px; color: #999; margin-top: 10px;">
              Debug: hasExistingApplication = {{ hasExistingApplication }}, applicationStatus = {{ applicationStatus ? 'exists' : 'null' }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/http'
import { getAuthUser } from '@/utils/auth-storage'

const router = useRouter()
const loading = ref(false)

// ✅ 이 페이지는 버튼 표시를 위한 로그인 여부만 필요
const isLoggedIn = ref(false)
const user = ref({ name: '' })

// 신청 현황
const hasExistingApplication = ref(false)
const showStatusModal = ref(false)
const applicationStatus = ref(null)

const form = reactive({
  businessName: '',
  hotelName: '',
  businessNumber: '',
  address: '',
  phone: ''
})

// 사업자등록번호 마스킹
const onBusinessNumberInput = (e) => {
  const digits = (e?.target?.value ?? '').replace(/\D/g, '').slice(0, 10)
  let formatted = ''
  if (digits.length <= 3)       formatted = digits
  else if (digits.length <= 5)  formatted = `${digits.slice(0,3)}-${digits.slice(3)}`
  else                          formatted = `${digits.slice(0,3)}-${digits.slice(3,5)}-${digits.slice(5)}`
  form.businessNumber = formatted
}

const submitApplication = async () => {
  loading.value = true
  try {
    if (hasExistingApplication.value === true) {
      loading.value = false
      showApplicationStatus()
      return alert('이미 신청 내역이 있습니다. 신청 현황을 확인해주세요.')
    }
    const bn = (form.businessNumber || '').trim()
    if (!/^\d{3}-\d{2}-\d{5}$/.test(bn)) {
      loading.value = false
      return alert('사업자 등록번호는 000-00-00000 형식으로 입력해주세요.')
    }
    const response = await api.post('/businesses/apply', form)
    alert(response.data.message || '신청이 완료되었습니다. 관리자 승인 후 활동할 수 있습니다.')
    await router.push('/')
  } catch (error) {
    if (error.response?.status === 401) {
      alert('인증이 필요합니다. 다시 로그인 후 시도해주세요.')
    } else if (error.response?.data?.error) {
      alert(error.response.data.error)
    } else {
      alert('신청 처리 중 오류가 발생했습니다. 다시 시도해주세요.')
    }
  } finally {
    loading.value = false
  }
}

const goBack = () => router.back()

const checkExistingApplication = async () => {
  if (!isLoggedIn.value) return
  try {
    const resp = await api.get('/owner/hotels/my-hotels')
    const list = Array.isArray(resp.data) ? resp.data : (resp.data?.data || [])
    if (Array.isArray(list) && list.length > 0) {
      const activeExists = list.some(x => (x.approvalStatus || x.status || 'PENDING') !== 'REJECTED')
      hasExistingApplication.value = activeExists
    } else {
      hasExistingApplication.value = false
    }
  } catch {
    hasExistingApplication.value = false
  }
}

const showApplicationStatus = async () => {
  showStatusModal.value = true
  applicationStatus.value = null
  hasExistingApplication.value = null
  try {
    const resp = await api.get('/owner/hotels/my-hotels')
    const list = Array.isArray(resp.data) ? resp.data : (resp.data?.data || [])
    if (Array.isArray(list) && list.length > 0) {
      const h = list.find(x => (x.approvalStatus || x.status) === 'PENDING') || list[0]
      applicationStatus.value = {
        id: h.id,
        name: h.name,
        address: h.address,
        businessName: h.businessName || null,
        businessId: h.businessId || null,
        phone: h.phone || null,
        status: h.approvalStatus || h.status || 'PENDING',
        createdAt: h.createdAt || null,
        approvalDate: h.approvalDate || null,
        rejectionReason: h.rejectionReason || null,
      }
      const activeExists = list.some(x => (x.approvalStatus || x.status || 'PENDING') !== 'REJECTED')
      hasExistingApplication.value = activeExists
    } else {
      applicationStatus.value = null
      hasExistingApplication.value = false
    }
  } catch {
    applicationStatus.value = null
    hasExistingApplication.value = false
  }
}

const canceling = ref(false)
const cancelApplication = async () => {
  if (!applicationStatus.value?.id) return
  if (!confirm('정말로 사업자 신청을 취소하시겠습니까? 신청 정보가 삭제됩니다.')) return
  canceling.value = true
  try {
    await api.delete(`/owner/hotels/my-hotels/${applicationStatus.value.id}`)
    alert('신청이 취소되었습니다.')
    applicationStatus.value = null
    hasExistingApplication.value = false
    showStatusModal.value = false
    await checkExistingApplication()
    form.businessName = ''
    form.hotelName = ''
    form.businessNumber = ''
    form.address = ''
    form.phone = ''
  } catch (e) {
    const msg = e?.response?.data?.message || '신청 취소에 실패했습니다.'
    alert(msg)
  } finally {
    canceling.value = false
  }
}

const closeStatusModal = () => { showStatusModal.value = false }

const getStatusText = (status) => ({
  PENDING: '승인 대기',
  APPROVED: '승인 완료',
  REJECTED: '반려됨',
  SUSPENDED: '정지됨'
}[status] || '알 수 없음')

const getStatusClass = (s) => `status-${(s||'').toLowerCase()}`

const formatDate = (d) => {
  if (!d) return '-'
  const date = new Date(d)
  return date.toLocaleDateString('ko-KR', { year:'numeric', month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit' })
}

const formatBusinessNumber = (input) => {
  if (input === null || input === undefined) return '-'
  const digits = String(input).replace(/\D/g, '')
  if (!digits) return '-'
  const s = digits.length === 10 ? digits : (digits.length < 10 ? digits.padStart(10,'0') : digits.slice(-10))
  return `${s.slice(0,3)}-${s.slice(3,5)}-${s.slice(5)}`
}

onMounted(async () => {
  const userData = getAuthUser()
  isLoggedIn.value = !!userData
  if (userData) user.value = userData
  if (isLoggedIn.value) await checkExistingApplication()
})
</script>

<style scoped src="@/assets/css/homepage/business-apply.css"></style>
