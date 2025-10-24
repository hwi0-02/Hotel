<template>
  <div class="owner-dashboard">
    <h2>대시보드</h2>

    <div class="sales-summary">
      <div class="sales-card">
        <h3>오늘 매출</h3>
        <p>{{ formatCurrency(sales.todaySales) }}</p>
        <div class="percentage-change" :class="getChangeClass(sales.todaySalesChange)">
          <small>전일 대비 </small>
          <span v-if="sales.todaySalesChange !== null">
            {{ formatPercentage(sales.todaySalesChange) }}
          </span>
        </div>
      </div>
      <div class="sales-card">
        <h3>이번 주 매출</h3>
        <p>{{ formatCurrency(sales.weeklySales) }}</p>
        <div class="percentage-change" :class="getChangeClass(sales.weeklySalesChange)">
          <small>전주 대비 </small>
          <span v-if="sales.weeklySalesChange !== null">
            {{ formatPercentage(sales.weeklySalesChange) }}
          </span>
        </div>
      </div>
      <div class="sales-card">
        <h3>이번 달 매출</h3>
        <p>{{ formatCurrency(sales.monthlySales) }}</p>
        <div class="percentage-change" :class="getChangeClass(sales.monthlySalesChange)">
          <small>전월 대비 </small>
          <span v-if="sales.monthlySalesChange !== null">
            {{ formatPercentage(sales.monthlySalesChange) }}
          </span>
        </div>
      </div>
    </div>

    <div class="chart-container">
      <h3>매출 그래프</h3>
      <div class="filters">
        <select v-model="filter.roomId" @change="fetchChartData">
          <option :value="null">전체 객실</option>
          <option v-for="room in roomTypes" :key="room.roomId" :value="room.roomId">
            {{ room.roomType }}
          </option>
        </select>
        
        <button @click="applyPeriodFilter('7d')" :class="{ active: filter.period === '7d' }">최근 7일</button>
        <button @click="applyPeriodFilter('30d')" :class="{ active: filter.period === '30d' }">최근 30일</button>
        <button @click="applyPeriodFilter('1y')" :class="{ active: filter.period === '1y' }">최근 1년</button>
        <button @click="applyPeriodFilter('5y')" :class="{ active: filter.period === '5y' }">최근 5년</button>
        <button @click="applyPeriodFilter('10y')" :class="{ active: filter.period === '10y' }">최근 10년</button>
        
        <input type="date" v-model="filter.startDate" @change="handleDateChange">
        <input type="date" v-model="filter.endDate" @change="handleDateChange">
        
        <button @click="resetFilters">초기화</button>
      </div>
      <SalesChart v-if="chartData" :chart-data="chartData" />
    </div>
  </div>
</template>

<script>
import SalesChart from './SalesChart.vue';
import http from '@/api/http';

export default {
  name: 'OwnerDashboard',
  components: {
    SalesChart,
  },
  // --- 이 data() 함수가 빠져 있었습니다 ---
  data() {
    return {
      sales: {
        todaySales: 0,
        weeklySales: 0,
        monthlySales: 0,
        todaySalesChange: null,
        weeklySalesChange: null,
        monthlySalesChange: null,
      },
      filter: {
        roomId: null,
        startDate: '',
        endDate: '',
        period: '7d',
      },
      roomTypes: [],
      chartData: null,
    };
  },
  methods: {
    formatCurrency(value) {
      if (value === null || value === undefined || isNaN(value)) {
        return '₩0';
      }
      return new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(value);
    },
    formatPercentage(value) {
      if (value === null || value === undefined) return '';
      const fixedValue = value.toFixed(1);
      return (value >= 0 ? '▲ +' : '▼ ') + fixedValue + '%';
    },
    getChangeClass(value) {
      if (value === null || value === undefined || value === 0) return 'neutral';
      return value > 0 ? 'positive' : 'negative';
    },
    async fetchSalesData() {
      try {
        const response = await http.get('/owner/dashboard/sales');
        this.sales = response.data;
      } catch (error) {
        console.error('Error fetching sales data:', error);
      }
    },
    async fetchChartData() {
      try {
        const params = {};
        if (this.filter.period) {
          params.period = this.filter.period;
        }
        if (this.filter.startDate && this.filter.endDate) {
          params.startDate = this.filter.startDate;
          params.endDate = this.filter.endDate;
        }
        if (this.filter.roomId) {
          params.roomId = this.filter.roomId;
        }

        const response = await http.get('/owner/dashboard/chart', { params });
        
        this.chartData = {
          labels: response.data.labels,
          datasets: [
            {
              label: '매출',
              backgroundColor: '#42A5F5',
              data: response.data.data,
            },
          ],
        };
      } catch (error) {
        console.error('Error fetching chart data:', error);
        this.chartData = null;
      }
    },
    applyPeriodFilter(period) {
      this.filter.period = period;
      this.filter.startDate = '';
      this.filter.endDate = '';
      this.fetchChartData();
    },
    handleDateChange() {
      if (this.filter.startDate && this.filter.endDate) {
        this.filter.period = null;
        this.fetchChartData();
      }
    },
    resetFilters() {
      this.filter.roomId = null;
      this.filter.startDate = '';
      this.filter.endDate = '';
      this.filter.period = '7d';
      this.fetchChartData();
    },
  },
  created() {
    this.fetchSalesData();
    this.fetchChartData();
  },
};
</script>

<style scoped>
/* 전체 대시보드 레이아웃 */
.owner-dashboard {
  padding: 2.5rem;
  background-color: #f8f9fa; /* 부드러운 회색 배경 */
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 대시보드 제목 */
h2 {
  font-size: 2rem;
  font-weight: 600;
  color: #343a40;
  margin-bottom: 2rem;
}

/* 상단 매출 요약 카드 컨테이너 */
.sales-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 3개의 카드를 동일한 너비로 배치 */
  gap: 2rem; /* 카드 사이의 간격 */
  margin-bottom: 3rem;
}

/* 개별 매출 카드 */
.sales-card {
  background-color: #ffffff;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06); /* 입체감을 주는 그림자 효과 */
  border: none; /* 기존 테두리 제거 */
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.sales-card:hover {
  transform: translateY(-5px); /* 마우스 호버 시 살짝 위로 이동 */
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
}

.sales-card h3 {
  font-size: 1rem;
  font-weight: 500;
  color: #6c757d; /* 제목 텍스트 색상 */
  margin: 0 0 0.5rem 0;
}

.sales-card p {
  font-size: 2.2rem; /* 공간 확보를 위해 매출 폰트 크기 살짝 조정 */
  font-weight: 700;
  color: #212529; /* 매출 텍스트 색상 */
  margin: 0;
  margin-bottom: 0.5rem;
}

.percentage-change {
  font-size: 1.1rem;
  font-weight: 600;
  display: block;
  flex-direction: column;
  align-items: center;
}

.percentage-change small {
  font-size: 0.9rem;
  font-weight: 400;
  color: #6c757d;
  margin-top: 0.2rem;
}

.percentage-change.positive {
  color: #007bff; /* 파란색으로 상승 표시 */
}

.percentage-change.negative {
  color: #dc3545; /* 빨간색으로 하락 표시 */
}

.percentage-change.neutral {
  color: #6c757d; /* 회색으로 변화 없음 표시 */
}

/* 차트 컨테이너 */
.chart-container {
  background-color: #ffffff;
  border-radius: 12px;
  padding: 2rem 2.5rem;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
  border: none;
}

.chart-container h3 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 2rem;
  color: #343a40;
}

/* 필터 영역 */
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem; /* 필터 요소 간의 간격 */
  align-items: center;
  margin-bottom: 2.5rem;
}

/* 필터 버튼, 선택창, 날짜 입력창 공통 스타일 */
.filters button,
.filters select,
.filters input[type="date"] {
  padding: 0.7rem 1.2rem;
  font-size: 0.9rem;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  background-color: #fff;
  transition: all 0.2s ease;
  color: #495057;
}

.filters button {
  cursor: pointer;
  background-color: #f8f9fa;
}

.filters button:hover {
  background-color: #e9ecef; /* 버튼 호버 색상 */
  border-color: #adb5bd;
}

/* 활성 버튼 스타일 */
.filters button.active {
  background-color: #007bff;
  color: white;
  border-color: #007bff;
}

.filters select:focus,
.filters input[type="date"]:focus {
  outline: none;
  border-color: #80bdff; /* 포커스 시 테두리 색상 */
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}
</style>