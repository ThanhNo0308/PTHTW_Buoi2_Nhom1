import React, { useEffect, useState } from 'react';
import { ListGroup, Form, InputGroup, Spinner } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSearch, faUserCircle, faCircle } from '@fortawesome/free-solid-svg-icons';
import { collection, query, where, orderBy, onSnapshot } from 'firebase/firestore';
import { db } from '../configs/FirebaseConfig';
import '../assets/css/FireBase.css';
import moment from 'moment';
import 'moment/locale/vi';

const ContactList = ({ contacts, selectedContact, onSelectContact, currentUser, loading }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredContacts, setFilteredContacts] = useState([]);
  const [onlineUsers, setOnlineUsers] = useState({});
  const [lastSeenTimes, setLastSeenTimes] = useState({});

  // Lọc danh sách liên hệ khi search term thay đổi
  useEffect(() => {
    if (!contacts) return;

    const filtered = contacts.filter(contact => {
      return (
        contact.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (contact.studentCode && contact.studentCode.toLowerCase().includes(searchTerm.toLowerCase()))
      );
    });


    setFilteredContacts(filtered);
  }, [contacts, searchTerm]);

  // Theo dõi trạng thái online của người dùng
  useEffect(() => {
    const onlineStatusRef = collection(db, 'online_status');

    const unsubscribe = onSnapshot(query(onlineStatusRef), (snapshot) => {
      const onlineStatusData = {};
      const lastSeenData = {};

      snapshot.docs.forEach(doc => {
        const data = doc.data();
        onlineStatusData[doc.id] = data.online || false;
        lastSeenData[doc.id] = data.lastSeen?.toDate() || null;
      });
      setOnlineUsers(onlineStatusData);
      setLastSeenTimes(lastSeenData);
    });

    return () => unsubscribe();
  }, []);

  // Theo dõi tin nhắn chưa đọc
  useEffect(() => {
    if (!currentUser || !contacts.length) return;

    // Tạo một listener cho mỗi cuộc trò chuyện để đếm tin nhắn chưa đọc
    const unsubscribes = contacts.map(contact => {
      const chatId = getChatId(currentUser.id, contact.id);
      const messagesRef = collection(db, 'chats', chatId, 'messages');

      return onSnapshot(
        query(messagesRef, where('read', '==', false), where('senderId', '==', contact.id)),
        (snapshot) => {
          const unreadCount = snapshot.docs.length;

          // Cập nhật lại contacts với số tin nhắn chưa đọc mới
          setFilteredContacts(prev => {
            const updated = prev.map(c => {
              if (c.id === contact.id) {
                return { ...c, unreadCount };
              }
              return c;
            });
            
            // Sắp xếp lại để người có tin nhắn mới lên đầu
            return [...updated].sort((a, b) => {
              if ((a.unreadCount || 0) > 0 && (b.unreadCount || 0) === 0) return -1;
              if ((a.unreadCount || 0) === 0 && (b.unreadCount || 0) > 0) return 1;
              return a.name.localeCompare(b.name);
            });
          });
        }
      );
    });

    // Cleanup listeners
    return () => {
      unsubscribes.forEach(unsub => unsub());
    };
  }, [contacts, currentUser]);

  // Tạo ID cuộc trò chuyện từ ID của 2 người dùng
  const getChatId = (uid1, uid2) => {
    return uid1 < uid2 ? `${uid1}_${uid2}` : `${uid2}_${uid1}`;
  };

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center h-100">
        <Spinner animation="border" variant="primary" />
      </div>
    );
  }

  return (
    <div className="contacts-wrapper">
      <div className="search-box p-2">
        <InputGroup>
          <InputGroup.Text id="search-addon">
            <FontAwesomeIcon icon={faSearch} />
          </InputGroup.Text>
          <Form.Control
            type="text"
            placeholder="Tìm kiếm..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </InputGroup>
      </div>

      <ListGroup className="contacts-list">
        {filteredContacts.length > 0 ? (
          filteredContacts.map(contact => (
            <ListGroup.Item
              key={contact.id}
              className={`contact-item d-flex align-items-center ${selectedContact?.id === contact.id ? 'active' : ''} ${contact.unreadCount > 0 ? 'has-unread' : ''}`}
              action
              onClick={() => onSelectContact(contact)}
              data-role={contact.role || 'Unknown'}
            >
              <div className="contact-avatar-container me-2 position-relative">
                {contact.image ? (
                  <img
                    src={contact.image}
                    alt={contact.name}
                    className="contact-avatar rounded-circle"
                  />
                ) : (
                  <FontAwesomeIcon
                    icon={faUserCircle}
                    size="2x"
                    className="contact-avatar-icon"
                  />
                )}
                <span className={`online-indicator ${onlineUsers[contact.id] ? 'online' : 'offline'}`}>
                  <FontAwesomeIcon icon={faCircle} size="xs" />
                </span>
              </div>
              <div className="contact-info flex-grow-1">
                <div className="d-flex justify-content-between align-items-center">
                  <h6 className={`contact-name mb-0 ${contact.unreadCount > 0 ? 'fw-bold' : ''}`}>{contact.name}</h6>
                  {contact.unreadCount > 0 && (
                    <span className="unread-badge badge rounded-pill bg-primary">{contact.unreadCount}</span>
                  )}
                </div>
                <p className="contact-status small mb-0">
                  {contact.role === 'Student' ? (
                    <span className="badge bg-primary me-1">Sinh viên</span>
                  ) : contact.role === 'Teacher' ? (
                    <span className="badge bg-success me-1">Giảng viên</span>
                  ) : (
                    <span className="badge bg-secondary me-1">{contact.role || 'Người dùng'}</span>
                  )}

                  {/* Hiển thị số tin nhắn chưa đọc cạnh role */}
                  {contact.unreadCount > 0 && (
                    <span className="badge bg-danger ms-1">{contact.unreadCount} tin mới</span>
                  )}
                </p>
                <p className="contact-status small mb-0">
                  {contact.studentCode
                    ? `MSSV: ${contact.studentCode}`
                    : contact.email
                      ? `Email: ${contact.email}`
                      : contact.username
                        ? `ID: ${contact.username}`
                        : ''
                  }
                </p>
              </div>
            </ListGroup.Item>
          ))
        ) : (
          <div className="text-center p-3 text-muted">
            {searchTerm
              ? 'Không tìm thấy liên hệ phù hợp'
              : 'Không có liên hệ nào'}
          </div>
        )}
      </ListGroup>
    </div>
  );
};

export default ContactList;