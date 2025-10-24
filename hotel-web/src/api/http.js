// src/api/http.js
import axios from "axios";

const baseURL = "http://localhost:8888/api";

const http = axios.create({
  baseURL,
  headers: { "Content-Type": "application/json" },
  withCredentials: true,
});

export default http;