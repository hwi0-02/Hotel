<template>
  <div class="main-page">
    <!-- Hero 배너 -->
    <div class="hero-section hero-bg">
      <div class="hero-overlay"></div>
      <div class="hero-text-container">
        <div class="hero-text">
          <p class="hero-subtitle">
            검색을 통해 요금을 비교하고 무료 취소 포함한 특가도 확인하세요!
          </p>
        </div>
      </div>
    </div>

    <!-- 검색 폼 -->
    <SearchForm />

    <!-- 추천 국내 여행지 -->
    <section class="recommended-section" v-if="showDomesticSection">
      <h2 class="section-title">추천 국내 여행지</h2>
      <div class="swiper-container">
        <button 
          v-show="domesticSwiper && !domesticSwiper.isBeginning"
          @click="prevDomestic"
          class="nav-button nav-button-left"
        >‹</button>

        <swiper
          :modules="modules"
          :slides-per-view="4"
          :space-between="20"
          :loop="false"
          :speed="600"
          :observer="true"
          :observe-parents="true"
          :update-on-window-resize="true"
          :watch-overflow="true"
          :pagination="{ clickable: true }"
          :breakpoints="{ 0: { slidesPerView: 1 }, 640: { slidesPerView: 2 }, 1024: { slidesPerView: 4 } }"
          class="destination-swiper"
          @swiper="(s) => domesticSwiper = s"
        >
          <swiper-slide
            v-for="(place, i) in destinationsDomestic"
            :key="i"
            class="destination-card"
            @click="searchDestination(place.name)"
            role="button"
            tabindex="0"
          >
            <img
              :src="place.image"
              :alt="place.name"
              class="destination-image"
              loading="lazy"
              decoding="async"
              fetchpriority="low"
              width="400"
              height="180"
              sizes="(max-width:640px) 100vw, (max-width:1024px) 50vw, 25vw"
            />
            <div class="destination-info">
              <h3>{{ place.name }}</h3>
              <p>{{ place.description }}</p>
            </div>
          </swiper-slide>
        </swiper>

        <button 
          v-show="domesticSwiper && !domesticSwiper.isEnd"
          @click="nextDomestic"
          class="nav-button nav-button-right"
        >›</button>
      </div>
    </section>

    <!-- 추천 해외 여행지 -->
    <section class="recommended-section" v-if="showOverseasSection">
      <h2 class="section-title">추천 해외 여행지</h2>
      <div class="swiper-container">
        <button 
          v-show="overseasSwiper && !overseasSwiper.isBeginning"
          @click="prevOverseas"
          class="nav-button nav-button-left"
        >‹</button>

        <swiper
          :modules="modules"
          :slides-per-view="4"
          :space-between="20"
          :loop="false"
          :speed="600"
          :observer="true"
          :observe-parents="true"
          :update-on-window-resize="true"
          :watch-overflow="true"
          :pagination="{ clickable: true }"
          :breakpoints="{ 0: { slidesPerView: 1 }, 640: { slidesPerView: 2 }, 1024: { slidesPerView: 4 } }"
          class="destination-swiper"
          @swiper="(s) => overseasSwiper = s"
        >
          <swiper-slide
            v-for="(place, i) in destinationsOverseas"
            :key="i"
            class="destination-card"
            @click="searchDestination(place.name)"
            role="button"
            tabindex="0"
          >
            <img
              :src="place.image"
              :alt="place.name"
              class="destination-image"
              loading="lazy"
              decoding="async"
              fetchpriority="low"
              width="400"
              height="180"
              sizes="(max-width:640px) 100vw, (max-width:1024px) 50vw, 25vw"
            />
            <div class="destination-info">
              <h3>{{ place.name }}</h3>
              <p>{{ place.description }}</p>
            </div>
          </swiper-slide>
        </swiper>

        <button 
          v-show="overseasSwiper && !overseasSwiper.isEnd"
          @click="nextOverseas"
          class="nav-button nav-button-right"
        >›</button>
      </div>
    </section>

    <!-- 인기 호텔 -->
    <div ref="hotelSentinel" style="height:1px;"></div>
    <section class="hotel-section" v-if="showHotelSection">
      <h2 class="section-title">인기 호텔 추천</h2>
      <div v-if="loadingPopular" class="hotel-list">
        <div class="hotel-card" v-for="n in 4" :key="'skeleton-'+n">
          <div class="hotel-image" style="background:#f2f3f5"></div>
        </div>
      </div>

      <div v-else class="hotel-list">
        <div
          v-for="(hotel, i) in popularHotels"
          :key="hotel.id || i"
          class="hotel-card"
          @click="goHotelDetail(hotel.id)"
        >
          <img
            :src="hotel.image"
            :alt="hotel.name"
            class="hotel-image"
            loading="lazy"
            decoding="async"
            fetchpriority="low"
            width="400"
            height="160"
            sizes="(max-width:640px) 100vw, (max-width:1024px) 50vw, 33vw"
          />
          <div class="hotel-info">
            <h3>{{ hotel.name }}</h3>
            <p>{{ hotel.city }}</p>
            <p class="hotel-price">{{ hotel.price }}</p>
            <p class="hotel-rating" v-if="hotel.rating">⭐ {{ hotel.rating }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 여행 꿀팁 -->
    <div ref="tipsSentinel" style="height:1px;"></div>
    <section class="tips-section" v-if="showTipsSection">
      <h2 class="section-title">여행 꿀팁</h2>
      <div class="tips-list">
        <div v-for="(tip, i) in travelTips" :key="i" class="tip-card">
          <h3>{{ tip.title }}</h3>
          <p>{{ tip.description }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped src="@/assets/css/homepage/mainpage.css"></style>

<script>
import SearchForm from "@/components/user/main_page/SearchForm.vue";
import { Swiper, SwiperSlide } from "swiper/vue";
import { Navigation, Pagination } from "swiper/modules";
import HotelApi from "@/api/HotelApi";
import { getAuthUser } from "@/utils/auth-storage";
import { resolveBackendUrl } from "@/api/http";
import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/pagination";

export default {
  name: "MainPage",
  components: { SearchForm, Swiper, SwiperSlide },
  data() {
    return {
      isLoggedIn: false,
      user: { name: "홍길동" },
      destinationsDomestic: [
        { name: "서울", description: "한국의 수도, 쇼핑과 문화의 중심지", image: "/images/seoul.webp" },
        { name: "부산", description: "바다와 해운대, 맛있는 해산물", image: "/images/busan.webp" },
        { name: "제주", description: "힐링 여행지, 한라산과 올레길", image: "/images/jeju.webp" },
        { name: "강릉", description: "동해안의 바다와 커피 거리", image: "/images/gangreung.webp" },
        { name: "여수", description: "밤바다로 유명한 낭만 여행지", image: "/images/yeosu.webp" },
        { name: "전주", description: "한옥마을과 맛의 고장", image: "/images/jeonju.webp" },
        { name: "경주", description: "천년 고도의 역사 여행", image: "/images/gyeongju.webp" },
        { name: "인천", description: "국제공항과 차이나타운", image: "/images/incheon.webp" }
      ],
      destinationsOverseas: [
        { name: "도쿄", description: "일본의 수도, 전통과 현대의 조화", image: "/images/tokyo.webp" },
        { name: "파리", description: "낭만의 도시, 에펠탑과 루브르", image: "/images/paris.webp" },
        { name: "방콕", description: "태국의 활기찬 수도", image: "/images/bangkok.webp" },
        { name: "뉴욕", description: "세계의 중심, 자유의 여신상", image: "/images/newyork.webp" },
        { name: "런던", description: "영국의 수도, 빅벤과 타워브리지", image: "/images/london.webp" },
        { name: "로마", description: "고대 유적과 이탈리아 감성", image: "/images/roma.webp" },
        { name: "시드니", description: "오페라하우스와 아름다운 항구", image: "/images/sydney.webp" },
        { name: "하와이", description: "천국 같은 휴양지", image: "/images/hawaii.webp" }
      ],
      domesticSwiper: null,
      overseasSwiper: null,
      popularHotels: [],
      loadingPopular: true,
      travelTips: [
        { title: "제주도 여행 전 필수 체크리스트", description: "렌트카 예약, 숙소, 맛집 예약까지! 미리 준비하면 편리해요." },
        { title: "유럽 여행 시 꿀팁", description: "유레일 패스로 교통비 절약하고 인기 명소는 사전 예약 필수!" },
        { title: "해외여행 짐 싸기 노하우", description: "짐은 최소화! 멀티어댑터, 보조배터리는 필수 아이템." }
      ],
      showDomesticSection: true,
      showOverseasSection: true,
      showHotelSection: false,
      showTipsSection: false
    };
  },
  mounted() {
    this.checkAuthStatus();
    this.initSectionObservers();
  },
  methods: {
    checkAuthStatus() {
      const userInfo = getAuthUser();
      if (userInfo) {
        this.isLoggedIn = true;
        this.user = userInfo;
      }
    },
    handleLogout() {
      this.isLoggedIn = false;
      this.user = { name: "홍길동" };
    },
    async fetchPopularHotels() {
      try {
        this.loadingPopular = true;
        const page = await HotelApi.search({ page: 0, size: 20 });
        let items = Array.isArray(page?.content) ? page.content : [];
        items = items.sort((a, b) => (b.starRating || 0) - (a.starRating || 0)).slice(0, 4);
        this.popularHotels = items.map(it => ({
          id: it.id,
          name: it.name,
          city: it.address || "",
          lowestPrice: this.normalizePrice(it?.lowestPrice ?? it?.hotelPrice),
          price: "요금 정보 없음",
          rating: it?.starRating ?? null,
          image: this.resolveImage(it?.coverImage)
        }));
        this.popularHotels.forEach(hotel => {
          if (typeof hotel.lowestPrice === "number") {
            hotel.price = this.formatPrice(hotel.lowestPrice);
          }
        });
        await this.fillMissingHotelPrices(this.popularHotels);
      } finally {
        this.loadingPopular = false;
      }
    },
    async fillMissingHotelPrices(list) {
      const targets = list.filter(h => (h.lowestPrice == null || Number.isNaN(h.lowestPrice)) && h.id);
      if (!targets.length) return;
      await Promise.all(targets.map(async (hotel) => {
        try {
          const detail = await HotelApi.getDetail(hotel.id);
          const rooms = Array.isArray(detail?.rooms) ? detail.rooms : [];
          const min = rooms
            .map(r => this.normalizePrice(r?.price))
            .filter(v => typeof v === "number" && v > 0)
            .reduce((acc, v) => (acc == null ? v : Math.min(acc, v)), null);
          if (min != null) {
            hotel.lowestPrice = min;
            hotel.price = this.formatPrice(min);
          }
        } catch (err) {
          console.debug("[MainPage] failed to load hotel detail for price", hotel.id, err?.message);
        }
      }));
    },
    normalizePrice(raw) {
      const n = raw == null ? null : Number(raw);
      return Number.isFinite(n) && n > 0 ? n : null;
    },
    formatPrice(value) {
      return `₩${Number(value).toLocaleString()} / 1박`;
    },
    resolveImage(url) {
      if (!url) return "/images/paradiseHotel.webp";
      if (/^https?:\/\//i.test(url)) return url;
      const normalized = url.startsWith("/uploads/")
        ? url
        : `/uploads/${url.replace(/^\/+/, "")}`;
      return resolveBackendUrl(normalized);
    },
    goHotelDetail(id) {
      if (id) this.$router.push({ name: "HotelDetail", params: { id } });
    },
    initSectionObservers() {
      const opts = { root: null, rootMargin: "200px 0px", threshold: 0.01 };
      const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            if (entry.target === this.$refs.hotelSentinel) {
              this.showHotelSection = true;
              this.fetchPopularHotels();
            }
            if (entry.target === this.$refs.tipsSentinel) {
              this.showTipsSection = true;
            }
            observer.unobserve(entry.target);
          }
        });
      }, opts);
      if (this.$refs.hotelSentinel) observer.observe(this.$refs.hotelSentinel);
      if (this.$refs.tipsSentinel) observer.observe(this.$refs.tipsSentinel);
    },
    prevDomestic() { this.domesticSwiper?.slidePrev(); },
    nextDomestic() { this.domesticSwiper?.slideNext(); },
    prevOverseas() { this.overseasSwiper?.slidePrev(); },
    nextOverseas() { this.overseasSwiper?.slideNext(); },
    searchDestination(name) {
      const q = (name || '').toString().trim();
      if (!q) return;
      this.$router.push({ path: '/search', query: { q } });
    }
  },
  setup() {
    return { modules: [Navigation, Pagination] };
  }
};
</script>
