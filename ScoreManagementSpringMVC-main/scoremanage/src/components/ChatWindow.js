import React, { useState, useEffect, useRef } from 'react';
import { InputGroup, Form, Button, Spinner } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {faPaperPlane, faUserCircle, faEllipsisV, faFileUpload, faCheck, faCheckDouble,
  faFile, faTimes, faComments, faTrash, faCircle} from '@fortawesome/free-solid-svg-icons';
import { db} from '../configs/FirebaseConfig';
import { markMessageAsRead, deleteMessage } from '../configs/FirebaseUtils';
import { collection, query, orderBy, addDoc, serverTimestamp,
  onSnapshot, doc, setDoc} from 'firebase/firestore';
import moment from 'moment';
import 'moment/locale/vi';
import '../assets/css/FireBase.css';

const ChatWindow = ({ contact, currentUser }) => {
  const [message, setMessage] = useState('');
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [file, setFile] = useState(null);
  const [fileUploading, setFileUploading] = useState(false);
  const messagesEndRef = useRef(null);
  const fileInputRef = useRef(null);
  const chatWindowRef = useRef(null);
  const chatMessagesRef = useRef(null);
  const [contactStatus, setContactStatus] = useState({
    online: false,
    lastSeen: null
  });

  // Hiển thị thời gian hoạt động cuối cùng
  const formatLastSeen = (timestamp) => {
    if (!timestamp) return 'Chưa hoạt động';

    moment.locale('vi');
    const lastActive = moment(timestamp);
    const now = moment();

    if (now.diff(lastActive, 'seconds') < 60) {
      return 'Vừa mới truy cập';
    } else if (now.diff(lastActive, 'minutes') < 60) {
      return `Hoạt động ${now.diff(lastActive, 'minutes')} phút trước`;
    } else if (now.diff(lastActive, 'hours') < 24) {
      return `Hoạt động ${now.diff(lastActive, 'hours')} giờ trước`;
    } else if (now.diff(lastActive, 'days') < 7) {
      return `Hoạt động ${now.diff(lastActive, 'days')} ngày trước`;
    } else {
      return `Hoạt động ngày ${lastActive.format('DD/MM/YYYY')}`;
    }
  };

  // Theo dõi trạng thái online và lastSeen
  useEffect(() => {
    if (!contact || !contact.id) return;

    const userStatusRef = doc(db, 'online_status', contact.id);

    const unsubscribe = onSnapshot(userStatusRef, (doc) => {
      if (doc.exists()) {
        const data = doc.data();
        setContactStatus({
          online: data.online || false,
          lastSeen: data.lastSeen?.toDate() || null
        });
      }
    });

    return () => unsubscribe();
  }, [contact]);

  // Tạo ID cuộc trò chuyện từ ID của 2 người dùng
  const getChatId = (uid1, uid2) => {
    return uid1 < uid2 ? `${uid1}_${uid2}` : `${uid2}_${uid1}`;
  };

  // Theo dõi tin nhắn
  useEffect(() => {
    if (!currentUser || !contact) return;

    // Sử dụng username_role 
    const currentUserId = `${currentUser.id}_${currentUser.username}_${currentUser.role}`;
    const contactId = contact.id;

    const chatId = getChatId(currentUserId, contactId);

    const messagesRef = collection(db, 'chats', chatId, 'messages');

    // Kiểm tra ID người gửi
    const unsubscribe = onSnapshot(
      query(messagesRef, orderBy('timestamp', 'asc')),
      (snapshot) => {
        const messagesList = snapshot.docs.map(doc => ({
          id: doc.id,
          ...doc.data(),
          timestamp: doc.data().timestamp?.toDate() || new Date()
        }));
        setMessages(messagesList);
        setLoading(false);

        // Đánh dấu các tin nhắn từ người kia là đã đọc 
        snapshot.docs.forEach(doc => {
          const messageData = doc.data();
          if (messageData.senderId === contactId && !messageData.read) {
            markMessageAsRead(chatId, doc.id);
          }
        });
      }
    );

    // Cleanup listener
    return () => unsubscribe();
  }, [currentUser, contact]);

  // Gửi tin nhắn
  const handleSendMessage = async (e) => {
    e.preventDefault();

    if ((!message.trim() && !file) || sending) return;

    try {
      setSending(true);

      // Giữ các giá trị hiện tại
      const currentMessage = message;
      const currentFile = file;

      // Reset form
      setMessage('');
      setFile(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }

      // Các thông tin cần thiết
      const currentUserId = `${currentUser.id}_${currentUser.username}_${currentUser.role}`;
      const contactId = contact.id;
      const chatId = getChatId(currentUserId, contactId);

      // Xử lý file dưới dạng Base64
      let fileData = null;
      if (currentFile) {
        fileData = {
          fileName: currentFile.name,
          fileType: currentFile.type,
          fileSize: currentFile.size,
          fileContent: currentFile.base64
        };
      }

      // Tạo tin nhắn với fileData thay vì URL
      const messageData = {
        text: currentMessage.trim(),
        senderId: currentUserId,
        senderName: currentUser.name || currentUser.username,
        timestamp: serverTimestamp(),
        read: false,
        fileData: fileData // Lưu trực tiếp trong Firestore
      };

      // Thêm tin nhắn vào Firestore
      const messagesRef = collection(db, 'chats', chatId, 'messages');
      await addDoc(messagesRef, messageData);

      // Cập nhật metadata của cuộc trò chuyện
      const chatRef = doc(db, 'chats', chatId);
      await setDoc(chatRef, {
        participants: [currentUserId, contactId],
        lastMessageAt: serverTimestamp(),
        lastMessageText: currentMessage.trim() || (currentFile ? `Đã gửi ${currentFile.name}` : '')
      }, { merge: true });

    } catch (error) {
      console.error('Lỗi khi gửi tin nhắn:', error);
      alert('Không thể gửi tin nhắn. Vui lòng thử lại sau.');
    } finally {
      setSending(false);
    }
  };

  // Xử lý khi chọn file
  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      // Giới hạn kích thước file - 500KB là an toàn cho Firestore
      const MAX_FILE_SIZE = 500 * 1024; // 500KB
      if (selectedFile.size > MAX_FILE_SIZE) {
        alert(`File quá lớn. Vui lòng chọn file nhỏ hơn ${MAX_FILE_SIZE / 1024}KB`);
        e.target.value = "";
        return;
      }

      // Đọc file dưới dạng Base64
      const reader = new FileReader();
      reader.onload = (event) => {
        setFile({
          name: selectedFile.name,
          type: selectedFile.type,
          size: selectedFile.size,
          base64: event.target.result
        });
      };
      reader.readAsDataURL(selectedFile);
    }
  };

  // Hiển thị thời gian tin nhắn
  const formatMessageTime = (timestamp) => {
    if (!timestamp) return '';

    moment.locale('vi');
    const now = moment();
    const messageTime = moment(timestamp);

    if (now.clone().startOf('day').isSame(messageTime.clone().startOf('day'))) {
      // Cùng ngày - hiển thị thời gian kèm "Hôm nay"
      return `Hôm nay, ${messageTime.format('HH:mm')}`;
    } else if (now.clone().subtract(1, 'days').startOf('day').isSame(messageTime.clone().startOf('day'))) {
      // Ngày hôm qua
      return `Hôm qua, ${messageTime.format('HH:mm')}`;
    } else if (now.clone().diff(messageTime, 'days') < 7) {
      // Trong tuần - hiển thị thứ
      return `${messageTime.format('dddd')}, ${messageTime.format('HH:mm')}`;
    } else {
      // Trên 1 tuần - hiển thị ngày tháng
      return messageTime.format('DD/MM/YYYY, HH:mm');
    }
  };

  // Hiển thị thời gian divider
  const shouldShowTimeDivider = (currentMsg, prevMsg) => {
    if (!prevMsg) return true;

    const currentTime = moment(currentMsg.timestamp);
    const prevTime = moment(prevMsg.timestamp);

    // Hiển thị divider khi khác ngày hoặc cách nhau ít nhất 1 ngày
    return !currentTime.isSame(prevTime, 'day') ||
      Math.abs(currentTime.diff(prevTime, 'minutes')) >= 1440;
  };

  useEffect(() => {
    // Chỉ cuộn trong khung chat khi có tin nhắn mới
    if (chatMessagesRef.current && messages.length > 0) {
      chatMessagesRef.current.scrollTop = chatMessagesRef.current.scrollHeight;
    }
  }, [messages]);

  // Auto-scroll khi khung chat mới được mở
  useEffect(() => {
    if (chatMessagesRef.current) {
      chatMessagesRef.current.scrollTop = chatMessagesRef.current.scrollHeight;
    }
  }, [contact]);

  // Xóa tin nhắn
  const handleDeleteMessage = async (messageId, hasFile = false) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa tin nhắn này không?')) {
      try {
        // Sử dụng ID người dùng hiện tại và liên hệ để tạo chatId
        const currentUserId = `${currentUser.id}_${currentUser.username}_${currentUser.role}`;
        const contactId = contact.id;
        const chatId = getChatId(currentUserId, contactId);

        await deleteMessage(chatId, messageId, hasFile);
        // Không cần làm gì thêm vì tin nhắn sẽ tự động cập nhật qua onSnapshot
      } catch (error) {
        console.error('Lỗi khi xóa tin nhắn:', error);
        alert('Không thể xóa tin nhắn. Vui lòng thử lại sau.');
      }
    }
  };

  return (
    <div className="chat-window" ref={chatWindowRef}>
      <div className="chat-header d-flex justify-content-between align-items-center p-3">
        <div className="d-flex align-items-center">
          {contact.image ? (
            <img
              src={contact.image}
              alt={contact.name}
              className="contact-avatar rounded-circle me-2"
            />
          ) : (
            <FontAwesomeIcon
              icon={faUserCircle}
              size="2x"
              className="me-2 text-secondary"
            />
          )}
          <div>
            <div className="d-flex align-items-center">
              <h5 className="mb-0 me-2">{contact.name}</h5>
              {/* Hiển thị trạng thái online/offline */}
              <span className={`status-indicator ${contactStatus.online ? 'online' : 'offline'}`}>
                <FontAwesomeIcon icon={faCircle} size="xs" className="me-1" />
                {contactStatus.online ? 'Đang hoạt động' : formatLastSeen(contactStatus.lastSeen)}
              </span>
            </div>
            <small className="text-muted">
              {contact.studentCode
                ? `Sinh viên - MSSV: ${contact.studentCode}`
                : `Giảng viên - Email: ${contact.email}`}
            </small>
          </div>
        </div>
        <Button variant="light" className="rounded-circle">
          <FontAwesomeIcon icon={faEllipsisV} />
        </Button>
      </div>

      <div className="chat-messages p-3" ref={chatMessagesRef}>
        {loading ? (
          <div className="d-flex justify-content-center align-items-center h-100">
            <Spinner animation="border" variant="primary" />
          </div>
        ) : messages.length > 0 ? (
          <>
            {messages.map((msg, index) => {
              const isCurrentUser = msg.senderId === `${currentUser.id}_${currentUser.username}_${currentUser.role}`;
              const prevMsg = index > 0 ? messages[index - 1] : null;
              const showTimeDivider = shouldShowTimeDivider(msg, prevMsg);

              return (
                <React.Fragment key={msg.id}>
                  {showTimeDivider && (
                    <div className="time-divider">
                      <div className="time-divider-line"></div>
                      <span className="time-divider-text">
                        {moment(msg.timestamp).format('HH:mm D MMMM, YYYY')}
                      </span>
                      <div className="time-divider-line"></div>
                    </div>
                  )}
                  <div
                    className={`message-container ${isCurrentUser ? 'message-right' : 'message-left'}`}
                  >
                    <div className="message-content">
                      {!isCurrentUser && (
                        <div className="sender-name mb-1 fw-bold">{msg.senderName}</div>
                      )}

                      {msg.fileData && (
                        <div className="file-attachment mb-2">
                          {msg.fileData.fileType && msg.fileData.fileType.startsWith('image/') ? (
                            // Hiển thị hình ảnh
                            <img
                              src={msg.fileData.fileContent}
                              alt="Attached"
                              className="img-fluid rounded message-image"
                              style={{ maxWidth: '100%', maxHeight: '300px' }}
                            />
                          ) : (
                            // Hiển thị link tải xuống
                            <div className="file-download-link">
                              <FontAwesomeIcon icon={faFile} className="me-2" />
                              <a
                                href={msg.fileData.fileContent}
                                download={msg.fileData.fileName}
                                target="_blank"
                                rel="noopener noreferrer"
                              >
                                {msg.fileData.fileName || 'Tải tệp đính kèm'}
                              </a>
                              <span className="ms-2 text-muted small">
                                ({Math.round(msg.fileData.fileSize / 1024)} KB)
                              </span>
                            </div>
                          )}
                        </div>
                      )}

                      {msg.fileURL && !msg.fileData && (
                        <div className="file-attachment mb-2">
                          {msg.fileType?.startsWith('image/') ? (
                            <img
                              src={msg.fileURL}
                              alt="Attached"
                              className="img-fluid rounded message-image"
                            />
                          ) : (
                            <a
                              href={msg.fileURL}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="file-download-link"
                            >
                              <FontAwesomeIcon icon={faFile} className="me-2" />
                              {msg.fileName || 'Tải tệp đính kèm'}
                            </a>
                          )}
                        </div>
                      )}

                      {msg.text && <div className="message-text">{msg.text}</div>}

                      <div className="message-time">
                        {formatMessageTime(msg.timestamp)}
                        {isCurrentUser && (
                          <span className="message-read-status ms-2">
                            {msg.read ? (
                              <span className="read">
                                <FontAwesomeIcon icon={faCheckDouble} /> Đã đọc
                              </span>
                            ) : (
                              <span className="unread">
                                <FontAwesomeIcon icon={faCheck} /> Chưa đọc
                              </span>
                            )}
                          </span>
                        )}
                        {isCurrentUser && (
                          <button
                            className="delete-message-btn"
                            onClick={() => handleDeleteMessage(msg.id, !!(msg.fileData || msg.fileURL))}
                            title="Xóa tin nhắn"
                          >
                            <FontAwesomeIcon icon={faTrash} />
                          </button>
                        )}
                      </div>

                    </div>
                  </div>
                </React.Fragment>
              );
            })}
            <div ref={messagesEndRef} />
          </>
        ) : (
          <div className="no-messages text-center p-5">
            <FontAwesomeIcon icon={faComments} className="fs-1 text-muted" />
            <p className="mt-3">Chưa có tin nhắn nào. Hãy bắt đầu cuộc trò chuyện!</p>
          </div>
        )}
      </div>

      <div className="chat-input p-3">
        {file && (
          <div className="selected-file mb-2 p-2 rounded border">
            <div className="d-flex align-items-center justify-content-between">
              <div>
                <FontAwesomeIcon icon={faFile} className="me-2" />
                {file.name}
              </div>
              <Button
                variant="link"
                className="text-danger p-0"
                onClick={() => setFile(null)}
              >
                <FontAwesomeIcon icon={faTimes} />
              </Button>
            </div>
          </div>
        )}

        <Form onSubmit={handleSendMessage}>
          <InputGroup>
            <Button
              variant="outline-secondary"
              onClick={() => fileInputRef.current.click()}
              disabled={fileUploading}
            >
              <FontAwesomeIcon icon={faFileUpload} />
              <input
                type="file"
                ref={fileInputRef}
                onChange={handleFileChange}
                style={{ display: 'none' }}
              />
            </Button>

            <Form.Control
              type="text"
              placeholder="Nhập tin nhắn..."
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              disabled={sending}
            />

            <Button
              type="submit"
              variant="primary"
              disabled={(!message.trim() && !file) || sending || fileUploading}
            >
              {sending || fileUploading ? (
                <Spinner animation="border" size="sm" />
              ) : (
                <FontAwesomeIcon icon={faPaperPlane} />
              )}
            </Button>
          </InputGroup>
        </Form>
      </div>
    </div>
  );
};

export default ChatWindow;