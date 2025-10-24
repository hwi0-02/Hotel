<!-- src/views/owner/OwnerRoom.vue -->
<template>
  <div class="room-management-container">
    <header class="page-header">
      <h1 class="page-title">객실 관리</h1>
      <button @click="goToRegisterPage" class="btn-primary">＋ 새 객실 등록</button>
    </header>

    <div class="room-list-card">
      <table class="room-table">
        <thead>
          <tr>
            <th>객실명</th>
            <th>객실 타입</th>
            <th>보유 객실</th>
            <th>현재 이용</th>
            <th>기본/최대 인원</th>
            <th>정가 (1박)</th>
            <th>판매가 (1박)</th>
            <th>관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="room in rooms" :key="room.id">
            <td>{{ room.name }}</td>
            <td>{{ room.roomType || '—' }}</td>

            <td>{{ room.roomCount ?? 0 }}개</td>

            <!-- 오늘 기준 이용 객실/인원 -->
            <td>
              <div class="usage-cell">
                <div>
                  <span class="usage-dot"></span>
                  이용 객실 <b>{{ usageRooms[room.id] ?? 0 }}</b> / {{ room.roomCount ?? 0 }}
                </div>
                <div class="usage-sub">
                  이용 인원 <b>{{ usageGuests[room.id] ?? '—' }}</b>
                </div>
              </div>
            </td>

            <!-- 기본/최대 인원 -->
            <td>
              {{ room.capacityMin ?? '—' }} / {{ room.capacityMax ?? '—' }}
            </td>

            <!-- 정가 -->
            <td class="text-right">
              <span v-if="room.originalPrice" class="orig strike">
                {{ money(room.originalPrice) }}
              </span>
              <span v-else>—</span>
            </td>

            <!-- 판매가(+할인율) -->
            <td class="text-right">
              <span class="now">{{ money(room.price) }}</span>
              <span v-if="discountRate(room) > 0" class="badge-discount">
                -{{ discountRate(room) }}%
              </span>
            </td>

            <td>
              <button @click="goToUpdatePage(room.id)" class="btn-secondary btn-sm">수정</button>
              <button @click="deleteRoom(room.id)" class="btn-danger btn-sm">삭제</button>
            </td>
          </tr>

          <tr v-if="!rooms.length">
            <td colspan="8" class="no-data">등록된 객실이 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>

/**
 * 현재 이용 수 계산 방법
 * - 프론트에서 오늘~내일 구간으로 /reservations/findoverlap?roomIds=... 호출
 *   → 각 roomId 별 '이용 객실 수' Map 반환
 * - "이용 인원"은 선택 엔드포인트(/owner/reservations/inhouse-guests)
 *   가 있으면 사용, 없으면 자동으로 ‘—’ 표시
 */
export default {
  name: 'OwnerRoom',
  data() {
    return {
      rooms: [],
      usageRooms: {},   // roomId -> 오늘 이용 객실 수
      usageGuests: {},  // roomId -> 오늘 투숙 인원 수 (선택, 없으면 ‘—’)
      usageLoading: false,
    };
  },
  methods: {
    money(n) {
      if (n == null) return '—';
      const num = Number(n);
      return Number.isFinite(num) ? `₩ ${num.toLocaleString('ko-KR')}` : '—';
    },
    discountRate(room) {
      const o = Number(room.originalPrice);
      const p = Number(room.price);
      if (!Number.isFinite(o) || !Number.isFinite(p) || o <= 0 || p >= o) return 0;
      return Math.round((1 - (p / o)) * 100);
    },

    goToRegisterPage() {
      const hotelId = Number(this.$route.query.hotelId);
      this.$router.push({ name: 'OwnerRoomRegister', query: { hotelId } });
    },
    goToUpdatePage(roomId) {
      this.$router.push({
        name: 'OwnerRoomUpdate',
        params: { id: roomId },
        query: { hotelId: this.$route.query.hotelId }
      });
    },

    async deleteRoom(roomId) {
      if (!confirm('정말로 이 객실을 삭제하시겠습니까?')) return;
      try {
        await this.$axios.delete(`/owner/rooms/${roomId}`);
        alert('객실이 삭제되었습니다.');
        await this.fetchRooms(); // 목록 새로고침
      } catch (error) {
        console.error('객실 삭제 실패:', error);
        alert('객실 삭제에 실패했습니다.');
      }
    },

    async fetchRooms() {
      try {
        const hotelId = Number(this.$route.query.hotelId);

        if (!hotelId) {
          alert('hotelId가 없습니다. 호텔 목록에서 다시 진입해주세요.');
          this.$router.push({ name: 'OwnerHotelList' });
          return;
        }

        const { data } = await this.$axios.get('/owner/rooms', {
          params: { hotelId }
        });

        // 키 정규화(카멜/스네이크 혼용 대비)
        this.rooms = (data || []).map(r => ({
          id: r.id,
          name: r.name,
          roomType: r.roomType || r.room_type || '',
          roomCount: r.roomCount ?? r.room_count ?? 0,
          capacityMin: r.capacityMin ?? r.capacity_min ?? null,
          capacityMax: r.capacityMax ?? r.capacity_max ?? null,
          price: r.price ?? null,
          originalPrice: r.originalPrice ?? r.original_price ?? null,
        }));

        // 이용 현황 로딩
        await this.fetchUsage();
      } catch (error) {
        console.error('객실 목록을 불러오는 데 실패했습니다:', error);
        if (error?.response?.status === 401) {
          alert('로그인이 필요합니다.');
          this.$router.push('/login');
          return;
        }
        alert('객실 목록을 불러오는 중 오류가 발생했습니다.');
      }
    },

    /** 오늘 기준 이용 객실/인원 조회 */
    async fetchUsage() {
      try {
        this.usageLoading = true;
        const roomIds = this.rooms.map(r => r.id).filter(Boolean);
        if (!roomIds.length) { this.usageRooms = {}; this.usageGuests = {}; return; }

        // 오늘~내일 (체크인 ≤ 오늘 < 체크아웃)
        const today = new Date();
        const toYMD = d => d.toISOString().slice(0, 10);
        const ci = toYMD(today);
        const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000);
        const co = toYMD(tomorrow);

        // 1) 이용 객실 수
        const qsRooms = roomIds.map(id => `roomIds=${encodeURIComponent(id)}`).join('&');
        const { data: overlap } = await this.$axios.get(
          `/reservations/findoverlap?checkIn=${encodeURIComponent(ci)}&checkOut=${encodeURIComponent(co)}&${qsRooms}`
        );
        this.usageRooms = overlap || {};

        // 2) (선택) 이용 인원 수
        const hotelId = Number(this.$route.query.hotelId);
        try {
          const { data: guestsMap } = await this.$axios.get(
            `/owner/reservations/inhouse-guests?hotelId=${encodeURIComponent(hotelId)}&${qsRooms}`
          );
          this.usageGuests = (guestsMap && typeof guestsMap === 'object') ? guestsMap : {};
        } catch {
          this.usageGuests = {}; // 엔드포인트 없으면 ‘—’
        }
      } catch (e) {
        console.warn('[usage] 조회 실패', e);
        this.usageRooms = {};
        this.usageGuests = {};
      } finally {
        this.usageLoading = false;
      }
    }
  },

  mounted() {
    this.fetchRooms();
  }
};
</script>

<style scoped>
.room-management-container {
  padding: 40px;
  background-color: #f8f9fa;
  height: 100%;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}
.page-title {
  font-size: 28px;
  font-weight: 800;
  color: #212529;
}
.btn-primary {
  background-color: #4f46e5;
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}
.btn-primary:hover { background-color: #4338ca; }

.room-list-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #e9ecef;
}
.room-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}
.room-table th {
  padding: 16px;
  border-bottom: 2px solid #dee2e6;
  font-size: 14px;
  font-weight: 700;
  color: #495057;
  background-color: #f8f9fa;
}
.room-table td {
  padding: 16px;
  border-bottom: 1px solid #e9ecef;
  font-size: 15px;
  vertical-align: middle;
}
.room-table tbody tr:last-child td { border-bottom: none; }
.text-right { text-align: right; }

/* 버튼 */
.btn-secondary, .btn-danger {
  border: 1px solid #dee2e6;
  background-color: #fff;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  margin-right: 8px;
  transition: background-color 0.2s, color 0.2s;
}
.btn-danger { color: #dc3545; border-color: #dc3545; }
.btn-secondary:hover { background-color: #f1f3f5; }
.btn-danger:hover { background-color: #dc3545; color: white; }

.no-data {
  text-align: center;
  padding: 60px;
  color: #868e96;
}

/* 사용 현황 셀 */
.usage-cell { line-height: 1.2; }
.usage-dot {
  display:inline-block; width:8px; height:8px; border-radius:50%;
  background:#10b981; margin-right:6px; transform: translateY(-1px);
}
.usage-sub { color:#778; font-size: 13px; margin-top: 4px; }

/* 가격 표시 */
.orig.strike { color:#9aa3af; text-decoration: line-through; }
.now { font-weight: 800; }
.badge-discount {
  display:inline-block; margin-left:6px; padding:2px 6px; font-size:12px; font-weight:800;
  color:#0f5132; background:#d1e7dd; border:1px solid #badbcc; border-radius: 999px;
}
</style>
