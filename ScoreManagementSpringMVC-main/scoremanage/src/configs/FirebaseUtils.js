import { db, storage } from '../configs/FirebaseConfig';
import {collection, doc, setDoc, getDoc, addDoc, query, where, orderBy, 
  getDocs, updateDoc, serverTimestamp, onSnapshot,deleteDoc} from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL, deleteObject } from 'firebase/storage';
import { API, endpoints } from './Apis';

/**
 * Lưu thông tin người dùng vào Firestore
 */
export const saveUserToFirestore = async (user) => {
  if (!user) return;

  try {
    // Sử dụng API current-user để lấy đầy đủ thông tin
    let fullUserData = null;
    try {
      const response = await API.get(endpoints["current-user"]);
      if (response.data) {
        console.log("Dữ liệu từ API current-user:", response.data);
        fullUserData = response.data;
      }
    } catch (apiError) {
      console.error("Lỗi khi lấy dữ liệu từ API:", apiError);
    }

    const userRef = doc(db, 'users', user.id);
    const userSnapshot = await getDoc(userRef);

    // Khởi tạo dữ liệu người dùng với thông tin cơ bản
    const userData = {
      id: user.id,
      name: user.fullName || user.name || user.username || "",
      email: user.email || "",
      role: user.role || "",
      lastSeen: serverTimestamp(),
    };

    // Nếu có dữ liệu đầy đủ từ API, bổ sung vào
    if (fullUserData) {
      // Dữ liệu người dùng
      if (fullUserData.user) {
        userData.name = fullUserData.user.name || userData.name;
        userData.gender = fullUserData.user.gender;
        userData.hometown = fullUserData.user.hometown || "";
        userData.birthdate = fullUserData.user.birthdate || null;
        userData.phone = fullUserData.user.phone || "";
        userData.image = fullUserData.user.image || userData.avatar;
        userData.active = fullUserData.user.active || "Active";
      }
      
      // Thông tin theo vai trò
      if (fullUserData.roleSpecificInfo) {
        const roleInfo = fullUserData.roleSpecificInfo;
        
        if (user.role === 'Student') {
          userData.studentCode = roleInfo.studentId || roleInfo.id || "";
          
          if (roleInfo.classId) {
            userData.classId = roleInfo.classId.id || "";
            userData.className = roleInfo.classId.className || "";
          }
          
          userData.status = roleInfo.status || "Active";
        } 
        else if (user.role === 'Teacher') {
          userData.teacherName = roleInfo.teacherName || roleInfo.id || "";
          userData.teacherCode = roleInfo.id || "";
          
          if (roleInfo.departmentId) {
            userData.departmentId = roleInfo.departmentId.id || "";
            userData.departmentName = roleInfo.departmentId.departmentName || "";
          }
        }
      }
    } 
    // Fallback nếu API không trả về dữ liệu
    else {
      // Thêm các trường dựa vào vai trò, đảm bảo không có giá trị undefined
      if (user.role === 'Student') {
        userData.studentCode = user.studentId || user.username || "";
        userData.className = user.className || "";
      } else if (user.role === 'Teacher') {
        userData.teacherCode = user.teacherId || user.username || "";
        userData.department = user.department || "";
      }
    }

    // Lưu vào Firestore
    if (userSnapshot.exists()) {
      // Cập nhật thông tin hiện có nhưng không ghi đè các trường đặc biệt
      await updateDoc(userRef, {
        ...userData,
        createdAt: userSnapshot.data().createdAt
      });
    } else {
      // Tạo thông tin mới
      await setDoc(userRef, {
        ...userData,
        createdAt: serverTimestamp()
      });
    }

    // Cập nhật trạng thái online
    await updateOnlineStatus(user.id, true);

    console.log('User saved to Firestore with full data:', user.id);
  } catch (error) {
    console.error('Error saving user to Firestore:', error);
    throw error;
  }
};

/**
 * Lấy thông tin người dùng từ Firestore
 */
export const getUserFromFirestore = async (userId) => {
  if (!userId) return null;

  try {
    const userRef = doc(db, 'users', userId);
    const userSnapshot = await getDoc(userRef);

    if (userSnapshot.exists()) {
      return { id: userSnapshot.id, ...userSnapshot.data() };
    } else {
      return null;
    }
  } catch (error) {
    console.error('Error getting user from Firestore:', error);
    throw error;
  }
};

/**
 * Cập nhật trạng thái online của người dùng
 */
export const updateOnlineStatus = async (userId, isOnline = true) => {
  try {
    const userStatusRef = doc(db, 'online_status', userId);
    await setDoc(userStatusRef, {
      online: isOnline,
      lastSeen: serverTimestamp()
    }, { merge: true });
  } catch (error) {
    console.error('Error updating online status:', error);
  }
};

/**
 * Thiết lập xử lý khi người dùng offline
  */
export const setupPresence = (userId) => {
  if (!userId) return () => {};

  updateOnlineStatus(userId, true);

  // Thiết lập xử lý khi người dùng đóng tab hoặc rời trang
  const handleOffline = () => {
    updateOnlineStatus(userId, false);
  };

  window.addEventListener('beforeunload', handleOffline);
  window.addEventListener('unload', handleOffline);
  
  // Trả về hàm cleanup
  return () => {
    updateOnlineStatus(userId, false);
    window.removeEventListener('beforeunload', handleOffline);
    window.removeEventListener('unload', handleOffline);
  };
};

/**
 * Tạo ID cuộc trò chuyện từ ID của 2 người dùng
 */
export const getChatId = (uid1, uid2) => {
  return uid1 < uid2 ? `${uid1}_${uid2}` : `${uid2}_${uid1}`;
};

/**
 * Gửi tin nhắn mới
 */
export const sendMessage = async (senderId, receiverId, senderName, text, file = null) => {
  try {
    const chatId = getChatId(senderId, receiverId);
    const messagesRef = collection(db, 'chats', chatId, 'messages');
    
    let fileURL = null;
    let fileName = null;
    let fileType = null;
    
    // Xử lý tải file lên nếu có
    if (file) {
      const fileRef = ref(storage, `chat_files/${chatId}/${Date.now()}_${file.name}`);
      await uploadBytes(fileRef, file);
      fileURL = await getDownloadURL(fileRef);
      fileName = file.name;
      fileType = file.type;
    }
    
    // Thêm tin nhắn mới
    const messageData = {
      text: text.trim(),
      senderId,
      senderName,
      timestamp: serverTimestamp(),
      read: false,
      fileURL,
      fileName,
      fileType
    };
    
    const messageRef = await addDoc(messagesRef, messageData);

    // Cập nhật thông tin về cuộc trò chuyện (metadata)
    const chatRef = doc(db, 'chats', chatId);
    await setDoc(chatRef, {
      participants: [senderId, receiverId],
      lastMessageAt: serverTimestamp(),
      lastMessageText: text.trim() || (fileURL ? 'Đã gửi một tệp đính kèm' : ''),
      lastMessageSenderId: senderId
    }, { merge: true });

    return { id: messageRef.id, ...messageData };
  } catch (error) {
    console.error('Error sending message:', error);
    throw error;
  }
};

/**
 * Đánh dấu tin nhắn là đã đọc
 */
export const markMessageAsRead = async (chatId, messageId) => {
  try {
    const messageRef = doc(db, 'chats', chatId, 'messages', messageId);
    await updateDoc(messageRef, { read: true });
  } catch (error) {
    console.error('Error marking message as read:', error);
  }
};

/**
 * Đánh dấu tất cả tin nhắn của một người gửi là đã đọc
 */
export const markAllMessagesAsRead = async (userId, contactId) => {
  try {
    const chatId = getChatId(userId, contactId);
    const messagesRef = collection(db, 'chats', chatId, 'messages');
    
    const unreadMessages = query(
      messagesRef,
      where('senderId', '==', contactId),
      where('read', '==', false)
    );
    
    const snapshot = await getDocs(unreadMessages);
    
    // Cập nhật tất cả tin nhắn chưa đọc
    const updatePromises = snapshot.docs.map(doc => {
      return updateDoc(doc.ref, { read: true });
    });
    
    await Promise.all(updatePromises);
  } catch (error) {
    console.error('Error marking all messages as read:', error);
  }
};

/**
 * Xóa một tin nhắn
 */
export const deleteMessage = async (chatId, messageId, deleteFile = true) => {
  try {
    // Lấy thông tin tin nhắn trước khi xóa
    const messageRef = doc(db, 'chats', chatId, 'messages', messageId);
    const messageSnapshot = await getDoc(messageRef);
    
    if (messageSnapshot.exists()) {
      const messageData = messageSnapshot.data();
      
      // Xóa file đính kèm nếu có
      if (deleteFile && messageData.fileURL) {
        try {
          const fileRef = ref(storage, messageData.fileURL);
          await deleteObject(fileRef);
        } catch (fileError) {
          console.error('Error deleting file:', fileError);
        }
      }
      
      // Xóa tin nhắn
      await deleteDoc(messageRef);
    }
  } catch (error) {
    console.error('Error deleting message:', error);
    throw error;
  }
};

/**
 * Đếm số tin nhắn chưa đọc
 */
export const countUnreadMessages = async (userId, contactId) => {
  try {
    const chatId = getChatId(userId, contactId);
    const messagesRef = collection(db, 'chats', chatId, 'messages');
    
    const unreadQuery = query(
      messagesRef,
      where('senderId', '==', contactId),
      where('read', '==', false)
    );
    
    const snapshot = await getDocs(unreadQuery);
    return snapshot.docs.length;
  } catch (error) {
    console.error('Error counting unread messages:', error);
    return 0;
  }
};