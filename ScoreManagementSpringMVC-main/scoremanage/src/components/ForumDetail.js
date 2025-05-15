import React, { useState, useEffect, useContext } from 'react';
import { Container, Card, Button, Form, Alert, Spinner, Badge } from 'react-bootstrap';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { forumApis } from '../configs/Apis';
import { MyUserContext } from '../App';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faComments, faArrowLeft, faPaperPlane, faReply, faTrash, faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';
import "../assets/css/forum.css";

const ForumDetail = () => {
  const { forumId } = useParams();
  const [user] = useContext(MyUserContext);
  const navigate = useNavigate();
  const [forum, setForum] = useState(null);
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [commentTitle, setCommentTitle] = useState("");
  const [commentContent, setCommentContent] = useState("");
  const [replying, setReplying] = useState(null); // ID of comment being replied to
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadForumDetail();
  }, [forumId]);

  const loadForumDetail = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await forumApis.getForumDetail(forumId);

      if (response.data.success) {
        setForum(response.data.forum);
        setComments(response.data.comments);
      } else {
        setError(response.data.error || "Không thể tải thông tin diễn đàn");
      }
    } catch (err) {
      console.error("Error loading forum detail:", err);
      setError("Lỗi khi tải thông tin diễn đàn: " + (err.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitComment = async (e) => {
    e.preventDefault();

    if (!commentTitle.trim() || !commentContent.trim()) {
      setError("Vui lòng nhập đầy đủ tiêu đề và nội dung bình luận");
      return;
    }

    try {
      setSubmitting(true);
      setError("");

      const commentData = {
        title: commentTitle,
        content: commentContent,
        parentCommentId: replying // null if it's a top-level comment
      };

      const response = await forumApis.addComment(forumId, commentData);

      if (response.data.success) {
        // Reset form
        setCommentTitle("");
        setCommentContent("");
        setReplying(null);

        // Reload comments
        loadForumDetail();
      } else {
        setError(response.data.error || "Không thể thêm bình luận");
      }
    } catch (err) {
      console.error("Error adding comment:", err);
      setError("Lỗi khi thêm bình luận: " + (err.response?.data?.error || err.message));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteComment = async (commentId) => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa bình luận này không?")) {
      return;
    }

    try {
      setError("");

      const response = await forumApis.deleteComment(commentId);

      if (response.data.success) {
        // Reload comments
        loadForumDetail();
      } else {
        setError(response.data.error || "Không thể xóa bình luận");
      }
    } catch (err) {
      console.error("Error deleting comment:", err);
      setError("Lỗi khi xóa bình luận: " + (err.response?.data?.error || err.message));
    }
  };

  const formatDate = (dateString) => {
    const options = {
      year: 'numeric',
      month: 'numeric',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    };
    return new Date(dateString).toLocaleDateString('vi-VN', options);
  };

  // Organize comments into a tree structure
  const buildCommentTree = () => {
    const commentMap = {};
    const rootComments = [];

    // First pass: create a map of comments by ID
    comments.forEach(comment => {
      comment.children = [];
      commentMap[comment.id] = comment;
    });

    // Second pass: link comments to their parents
    comments.forEach(comment => {
      if (comment.parentCommentId) {
        const parentComment = commentMap[comment.parentCommentId.id];
        if (parentComment) {
          parentComment.children.push(comment);
        }
      } else {
        rootComments.push(comment);
      }
    });

    return rootComments;
  };

  // Render a single comment and its replies
  const renderComment = (comment, depth = 0) => {
    return (
      <div className={`mb-3 ${depth > 0 ? 'nested-comment' : ''} fade-in`} key={comment.id}>
        <Card className="comment-card">
          <Card.Header className="comment-header">
            <div>
              <strong className="comment-author">{comment.title}</strong>
              <span className="ms-2 text-muted">
                bởi {comment.userId?.name || comment.userId?.username || "Không xác định"}
              </span>
            </div>
            <Badge bg="secondary" className="forum-badge">{formatDate(comment.createdAt)}</Badge>
          </Card.Header>
          <Card.Body>
            <Card.Text>{comment.content}</Card.Text>
            <div className="comment-actions">
              <Button
                variant="outline-primary"
                size="sm"
                className="me-2"
                onClick={() => setReplying(comment.id)}
              >
                <FontAwesomeIcon icon={faReply} className="me-1" /> Trả lời
              </Button>

              {/* Only show delete button if user is the owner of the comment or admin */}
              {(user?.id === comment.userId?.id || user?.role === "Admin") && (
                <Button
                  variant="outline-danger"
                  size="sm"
                  onClick={() => handleDeleteComment(comment.id)}
                >
                  <FontAwesomeIcon icon={faTrash} className="me-1" /> Xóa
                </Button>
              )}
            </div>
          </Card.Body>
        </Card>

        {/* Render replies */}
        {comment.children && comment.children.map(child => renderComment(child, depth + 1))}
      </div>
    );
  };

  if (loading) {
    return (
      <Container className="text-center my-5">
        <Spinner animation="border" variant="primary" />
        <p className="mt-3">Đang tải thông tin diễn đàn...</p>
      </Container>
    );
  }

  if (!forum) {
    return (
      <Container className="my-5">
        <Alert variant="danger">
          Không tìm thấy diễn đàn. Diễn đàn có thể đã bị xóa hoặc bạn không có quyền truy cập.
        </Alert>
        <Button variant="primary" as={Link} to="/forums">
          <FontAwesomeIcon icon={faArrowLeft} className="me-2" />
          Quay lại danh sách diễn đàn
        </Button>
      </Container>
    );
  }

  return (
    <Container className="my-4">
      <Button variant="secondary" as={Link} to="/forums" className="mb-3">
        <FontAwesomeIcon icon={faArrowLeft} className="me-2" />
        Quay lại danh sách diễn đàn
      </Button>

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError("")}>
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          {error}
        </Alert>
      )}

      <Card className="mb-4 forum-detail-card">
        <Card.Header className="forum-detail-header">
          <div className="d-flex justify-content-between align-items-center">
            <h4 className="mb-0 forum-detail-title">{forum.title}</h4>
            <Badge bg="light" text="dark" className="forum-badge">{formatDate(forum.createdAt)}</Badge>
          </div>
        </Card.Header>
        <div className="forum-detail-metadata">
          <div className="d-flex flex-wrap gap-3">
            <div><strong>Môn học:</strong> {forum.subjectTeacherId?.subjectId?.subjectName || "Không xác định"}</div>
            <div><strong>Giảng viên:</strong> {forum.subjectTeacherId?.teacherId?.teacherName || "Không xác định"}</div>
            <div><strong>Người tạo:</strong> {forum.userId?.name || forum.userId?.username || "Không xác định"}</div>
          </div>
        </div>
        <Card.Body className="forum-detail-content">
          <div className="mb-3 text-muted">{forum.description}</div>
          <div>{forum.content}</div>
        </Card.Body>
      </Card>

      <h4 className="mb-3 comments-section-title">
        <FontAwesomeIcon icon={faComments} className="me-2" />
        Bình luận ({comments.length})
      </h4>

      {/* Comment form */}
      <Card className="mb-4 comment-form-card">
        <Card.Header className="bg-light">
          <h5 className="mb-0">
            {replying ? "Trả lời bình luận" : "Thêm bình luận mới"}
            {replying && (
              <Button
                variant="link"
                className="text-danger"
                size="sm"
                onClick={() => setReplying(null)}
              >
                Hủy
              </Button>
            )}
          </h5>
        </Card.Header>
        <Card.Body>
          <Form onSubmit={handleSubmitComment}>
            <Form.Group className="mb-3">
              <Form.Label>Tiêu đề</Form.Label>
              <Form.Control
                type="text"
                value={commentTitle}
                onChange={(e) => setCommentTitle(e.target.value)}
                placeholder="Nhập tiêu đề bình luận"
                required
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Nội dung</Form.Label>
              <Form.Control
                as="textarea"
                rows={4}
                value={commentContent}
                onChange={(e) => setCommentContent(e.target.value)}
                placeholder="Nhập nội dung bình luận"
                required
              />
            </Form.Group>
            <div className="text-end">
              <Button
                type="submit"
                variant="primary"
                disabled={submitting}
              >
                {submitting ? (
                  <>
                    <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" />
                    Đang gửi...
                  </>
                ) : (
                  <>
                    <FontAwesomeIcon icon={faPaperPlane} className="me-2" />
                    Gửi bình luận
                  </>
                )}
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>

      {/* Comments */}
      <div className="comments-section">
        {buildCommentTree().length > 0 ? (
          buildCommentTree().map(comment => renderComment(comment))
        ) : (
          <Alert variant="info">
            Chưa có bình luận nào. Hãy là người đầu tiên bình luận!
          </Alert>
        )}
      </div>
    </Container>
  );
};

export default ForumDetail;