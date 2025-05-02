import React, { useState, useEffect, useRef } from 'react';
import { InputGroup, Form, Button, Spinner } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faPaperPlane,
  faUserCircle,
  faEllipsisV,
  faFileUpload,
  faCheck,
  faCheckDouble,
  faFile,
  faTimes,
  faComments
} from '@fortawesome/free-solid-svg-icons';
import { db, storage } from '../configs/FirebaseConfig';
import { markMessageAsRead } from '../configs/FirebaseUtils';
import {
  collection, query, where, orderBy, addDoc, serverTimestamp,
  onSnapshot, doc, updateDoc
} from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';
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

  // Tạo ID cuộc trò chuyện từ ID của 2 người dùng
  const getChatId = (uid1, uid2) => {
    return uid1 < uid2 ? `${uid1}_${uid2}` : `${uid2}_${uid1}`;
  };

  // Theo dõi tin nhắn
  useEffect(() => {
    if (!currentUser || !contact) return;

    // Sử dụng username_role 
    const currentUserId = `${currentUser.username}_${currentUser.role}`;
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

    if ((!message.trim() && !file) || !currentUser || !contact) return;

    try {
      setSending(true);

      // ID sử dụng username_role 
      const currentUserId = `${currentUser.username}_${currentUser.role}`;
      const contactId = contact.id;

      const chatId = getChatId(currentUserId, contactId);

      const messagesRef = collection(db, 'chats', chatId, 'messages');

      let fileURL = null;
      let fileName = null;
      let fileType = null;

      if (file) {
        // Xử lý file 
        setFileUploading(true);
        const fileRef = ref(storage, `chat_files/${chatId}/${Date.now()}_${file.name}`);
        await uploadBytes(fileRef, file);
        fileURL = await getDownloadURL(fileRef);
        fileName = file.name;
        fileType = file.type;
        setFile(null);
        setFileUploading(false);
      }

      await addDoc(messagesRef, {
        text: message.trim(),
        senderId: currentUserId, // Sử dụng username_role
        senderName: currentUser.name,
        timestamp: serverTimestamp(),
        read: false,
        fileURL,
        fileName,
        fileType
      });

      setMessage('');
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    } catch (error) {
      console.error('Lỗi khi gửi tin nhắn:', error);
    } finally {
      setSending(false);
    }
  };

  // Xử lý khi chọn file
  const handleFileChange = (e) => {
    if (e.target.files[0]) {
      setFile(e.target.files[0]);
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
            <h5 className="mb-0">{contact.name}</h5>
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
              const isCurrentUser = msg.senderId === `${currentUser.username}_${currentUser.role}`;

              return (
                <div
                  key={msg.id}
                  className={`message-container ${isCurrentUser ? 'message-right' : 'message-left'}`}
                >
                  <div className="message-content">
                    {!isCurrentUser && (
                      <div className="sender-name mb-1 fw-bold">{msg.senderName}</div>
                    )}

                    {msg.fileURL && (
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
                    </div>
                  </div>
                </div>
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