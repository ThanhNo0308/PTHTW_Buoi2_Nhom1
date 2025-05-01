import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
import { getStorage } from "firebase/storage";
import { getAnalytics } from "firebase/analytics";

// Cấu hình Firebase
const firebaseConfig = {
  apiKey: "AIzaSyBqXOidj3hfsUPtgf31KKx5UyA_eRN0uSk",
  authDomain: "scoremanagement-8090f.firebaseapp.com",
  projectId: "scoremanagement-8090f",
  storageBucket: "scoremanagement-8090f.appspot.com",
  messagingSenderId: "912374481763",
  appId: "1:912374481763:web:05c255b949b566de9c8956",
  measurementId: "G-TCXRRETG2L"
};

// Khởi tạo Firebase
const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);
const auth = getAuth(app);
const db = getFirestore(app);
const storage = getStorage(app);

export { auth, db, storage, analytics };
export default app;