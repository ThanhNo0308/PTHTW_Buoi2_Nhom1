import React, { useState, useContext, useEffect } from 'react';
import { Container, Row, Col, Card, Alert } from 'react-bootstrap';
import { MyUserContext } from '../App';
import { useNavigate } from 'react-router-dom';
import ContactList from './ContactList';
import ChatWindow from './ChatWindow';
import { db } from '../configs/FirebaseConfig';
import { collection, query, where, orderBy, onSnapshot, getFirestore, enableNetwork, disableNetwork } from 'firebase/firestore';
import { saveUserToFirestore, setupPresence } from '../configs/FirebaseUtils';
import '../assets/css/FireBase.css';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faComments } from '@fortawesome/free-solid-svg-icons';
import defaultAvatar from '../assets/images/logo.png';

const ChatPage = () => {
  const [user] = useContext(MyUserContext);
  const navigate = useNavigate();
  const [selectedContact, setSelectedContact] = useState(null);
  const [contacts, setContacts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [networkStatus, setNetworkStatus] = useState(navigator.onLine);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!user) {
      const redirectTimer = setTimeout(() => {
        navigate('/login');
      }, 500); // Chờ 500ms để hiển thị thông báo trước khi chuyển hướng

      return () => clearTimeout(redirectTimer);
    }
  }, [user, navigate]);

  useEffect(() => {
    const handleOnline = () => {
      setNetworkStatus(true);
      enableNetwork(getFirestore());
      setError(null);
    };

    const handleOffline = () => {
      setNetworkStatus(false);
      disableNetwork(getFirestore());
      setError("Bạn đang ngoại tuyến. Vui lòng kiểm tra kết nối internet.");
    };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    if (!networkStatus) {
      setError("Bạn đang ngoại tuyến. Vui lòng kiểm tra kết nối internet.");
      setLoading(false);
      return;
    }

    // Đảm bảo thông tin của người dùng được lưu trong Firestore
    const ensureUserInFirestore = async () => {
      try {
        // Tạo một ID duy nhất từ username và role
        const userId = `${user.id}_${user.username}_${user.role}`;

        // Chuẩn bị dữ liệu người dùng
        const userData = {
          id: userId,
          username: user.username || "",
          fullName: user.fullName || user.name || "",
          email: user.email || "",
          role: user.role || "",
          avatar: user.avatar || user.image || defaultAvatar
        };

        // Nếu là sinh viên, thêm mã sinh viên
        if (user.role === 'Student') {
          userData.studentCode = user.studentCode || user.username || "";
          userData.className = user.className || "";
        }
        // Nếu là giảng viên, thêm mã giảng viên
        else if (user.role === 'Teacher') {
          userData.teacherCode = user.teacherId || user.username || "";
          userData.department = user.department || ""; // Thay vì undefined, dùng chuỗi rỗng
        }

        // Lưu thông tin vào Firestore
        await saveUserToFirestore(userData);

        // Thiết lập xử lý trạng thái online/offline
        return setupPresence(userId);
      } catch (error) {
        console.error('Error ensuring user in Firestore:', error);
        return () => {}; 
      }
    };

    // Tải danh sách liên hệ dựa vào vai trò
    const loadContacts = async () => {
      try {
        setLoading(true);
        const userId = `${user.id}_${user.username}_${user.role}`;

        // Tải tất cả người dùng, không phân biệt vai trò
        const contactsQuery = query(
          collection(db, 'users'),
          orderBy('name')
        );

        // Lắng nghe thay đổi real-time
        const unsubscribe = onSnapshot(contactsQuery, (snapshot) => {
          const contactsList = snapshot.docs
            .map(doc => ({
              id: doc.id,
              ...doc.data(),
              unreadCount: 0 // Số tin nhắn chưa đọc, sẽ cập nhật sau
            }))
            .filter(contact => contact.id !== userId); // Lọc bỏ user hiện tại

          setContacts(contactsList);
          setLoading(false);
        });

        return unsubscribe;
      } catch (error) {
        console.error('Lỗi khi tải danh sách liên hệ:', error);
        setLoading(false);
        return () => { };
      }
    };

    // Thiết lập và cleanup
    let cleanupPresence = () => { };
    let cleanupContacts = () => { };

    const setup = async () => {
      cleanupPresence = await ensureUserInFirestore();
      cleanupContacts = await loadContacts();
    };

    setup();

    return () => {
      cleanupPresence();
      cleanupContacts();
    };
  }, [user, navigate, networkStatus]);

  // Hàm xử lý khi chọn một liên hệ
  const handleSelectContact = async (contact) => {
    setSelectedContact(contact);
  };

  return (
    <Container fluid className="chat-container py-4">
      {error && <Alert variant="danger" className="mb-3">{error}</Alert>}
      <Card className="chat-card">
        <Card.Header className="chat-header">
          <h3>Tin nhắn</h3>
        </Card.Header>
        <Card.Body className="p-0">
          <Row className="g-0 h-100">
            {user ? (
              <>
                <Col md={4} className="contact-list-col">
                  <ContactList
                    contacts={contacts}
                    selectedContact={selectedContact}
                    onSelectContact={handleSelectContact}
                    currentUser={{
                      // Luôn sử dụng format chuỗi username_role cho tất cả người dùng
                      id: `${user.id}_${user.username}_${user.role}`,
                      ...user
                    }}
                    loading={loading}
                  />
                </Col>
                <Col md={8} className="chat-window-col">
                  {selectedContact ? (
                    <ChatWindow
                      contact={selectedContact}
                      currentUser={{
                        // Luôn sử dụng format chuỗi username_role cho tất cả người dùng
                        id: `${user.id}_${user.username}_${user.role}`,
                        ...user
                      }}
                    />
                  ) : (
                    <div className="no-chat-selected">
                      <div className="text-center p-5">
                        <FontAwesomeIcon icon={faComments} className="fs-1 text-muted" />
                        <p className="mt-3">Chọn một liên hệ để bắt đầu trò chuyện</p>
                      </div>
                    </div>
                  )}
                </Col>
              </>
            ) : (
              <Col className="text-center p-5">
                <div className="spinner-border text-primary" role="status">
                  <span className="visually-hidden">Đang chuyển hướng...</span>
                </div>
                <p className="mt-3">Đang chuyển hướng đến trang đăng nhập...</p>
              </Col>
            )}
          </Row>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default ChatPage;