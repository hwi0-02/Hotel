import http from './http';

export default {
  // JWT 토큰 헤더 포함 사용자 정보 조회
  getInfo: async () => {
    const res = await http.get('/user/info');
    return res.data;
  },

  // 특정 사용자 ID로 조회
  getById: async (userId) => {
    const res = await http.get(`/users/${userId}`);
    return res.data;
  },

  // 프로필 이미지 업데이트 (multipart 업로드)
  updateProfileImage: async (file) => {
    const formData = new FormData();
    formData.append('file', file);

    const res = await http.patch('/user/profile-image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
  },
};