import React, { useContext } from 'react';
import "../assets/css/base.css";
import "../assets/css/styles.css";
import logo from '../assets/images/logo.png';
import cookie from "react-cookies";
import { Container, Nav, Navbar, NavDropdown } from 'react-bootstrap';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { MyUserContext } from '../App';
import { faEdit } from '@fortawesome/free-solid-svg-icons/faEdit';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faHome, faChalkboardTeacher, faUserGraduate, faCogs, faChartBar,
  faSignInAlt, faUserPlus, faUserCircle, faSignOutAlt, faUser, faBookOpen,
  faSchool, faGraduationCap, faBuilding, faBook, faCalendarAlt,
  faTasks, faChartLine, faChartPie, faChartArea, faSearch, faFileImport, faClipboardList, faComments, faMessage
} from '@fortawesome/free-solid-svg-icons';


const Header = () => {
  const [user, dispatch] = useContext(MyUserContext);
  const navigate = useNavigate();

  const logout = () => {
    cookie.remove("user", { path: '/' });
    dispatch({
      type: "logout"
    });

    navigate('/login');
  };

  return (
    <header>
      <Navbar expand="lg" className="navbar-dark">
        <Container>
          <div className="header_nav_title d-flex align-items-center">
            <Link to="/" className="navbar-brand d-flex align-items-center">
              <img
                src={logo}
                alt="Logo"
                width="70"
                height="54"
                className="d-inline-block align-text-top me-2"
              />
              <div className="brand-text">
                <span className="fw-bold">Trường Đại học</span>
                <span className="fw-bolder text-warning"> MilkyWay</span>
              </div>
            </Link>
          </div>
          <Navbar.Toggle aria-controls="navbarNav" />
          <Navbar.Collapse id="navbarNav" className="justify-content-between">
            <Nav>
              {!user && (
                <Nav.Link as={NavLink} to="/">
                  <FontAwesomeIcon icon={faHome} className="me-1" /> Trang chủ
                </Nav.Link>
              )}
            </Nav>

            {/* Admin Navigation */}
            {user && user.role === 'Admin' && (
              <Nav className="mr-auto">
                <Nav.Link as={NavLink} to="/admin/pageAdmin">
                  <FontAwesomeIcon icon={faHome} className="me-1" /> Trang chủ
                </Nav.Link>
                <Nav.Link as={NavLink} to="/admin/teachers">
                  <FontAwesomeIcon icon={faChalkboardTeacher} className="me-1" /> Giảng viên
                </Nav.Link>
                <Nav.Link as={NavLink} to="/admin/students">
                  <FontAwesomeIcon icon={faUserGraduate} className="me-1" /> Sinh viên
                </Nav.Link>

                <NavDropdown
                  title={<><FontAwesomeIcon icon={faCogs} className="me-1" /> Quản lý học tập</>}
                  id="navbarDropdown1"
                >
                  <NavDropdown.Item as={NavLink} to="/admin/classes">
                    <FontAwesomeIcon icon={faSchool} className="me-2" /> Lớp học
                  </NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/admin/majors">
                    <FontAwesomeIcon icon={faGraduationCap} className="me-2" /> Ngành học
                  </NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/admin/departments">
                    <FontAwesomeIcon icon={faBuilding} className="me-2" /> Khoa
                  </NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/admin/subjects">
                    <FontAwesomeIcon icon={faBook} className="me-2" /> Môn học
                  </NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/admin/school-years">
                    <FontAwesomeIcon icon={faCalendarAlt} className="me-2" /> Năm học
                  </NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/admin/subjTeach">
                    <FontAwesomeIcon icon={faTasks} className="me-2" /> Phân công giảng dạy
                  </NavDropdown.Item>
                </NavDropdown>

                <NavDropdown
                  title={<><FontAwesomeIcon icon={faChartBar} className="me-1" /> Thống kê</>}
                  id="statisticsDropdown"
                >
                  <NavDropdown.Item as={NavLink} to="/admin/statistics/class">
                    <FontAwesomeIcon icon={faChartLine} className="me-2" /> Thống kê theo lớp
                  </NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/admin/statistics/subject">
                    <FontAwesomeIcon icon={faChartPie} className="me-2" /> Thống kê theo môn học
                  </NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/admin/statistics/department">
                    <FontAwesomeIcon icon={faChartBar} className="me-2" /> Thống kê theo khoa
                  </NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/admin/statistics/major">
                    <FontAwesomeIcon icon={faChartArea} className="me-2" /> Thống kê theo ngành
                  </NavDropdown.Item>
                </NavDropdown>
              </Nav>
            )}

            {/* Teacher Navigation */}
            {user && user.role === 'Teacher' && (
              <Nav className="mr-auto">
                <Nav.Link as={NavLink} to="/teacher/dashboard">
                  <FontAwesomeIcon icon={faHome} className="me-1" /> Trang chủ
                </Nav.Link>
                <Nav.Link as={NavLink} to="/teacher/classes">
                  <FontAwesomeIcon icon={faSchool} className="me-1" /> Lớp học
                </Nav.Link>
                <Nav.Link as={NavLink} to="/teacher/schedule">
                  <FontAwesomeIcon icon={faCalendarAlt} className="me-1" /> Lịch dạy
                </Nav.Link>
                <Nav.Link as={NavLink} to="/teacher/students/search">
                  <FontAwesomeIcon icon={faSearch} className="me-1" /> Tìm kiếm
                </Nav.Link>
                <Nav.Link as={NavLink} to="/teacher/scores/import">
                  <FontAwesomeIcon icon={faFileImport} className="me-1" /> Nhập điểm
                </Nav.Link>
                <Nav.Link as={NavLink} to="/forums">
                  <FontAwesomeIcon icon={faComments} className="me-1" />Diễn đàn
                </Nav.Link>
                {user && (
                  <Nav.Link as={NavLink} to="/chat" className="nav-link">
                    <FontAwesomeIcon icon={faMessage} className="me-1" /> Tin nhắn
                  </Nav.Link>
                )}
              </Nav>
            )}

            {/* Student Navigation */}
            {user && user.role === 'Student' && (
              <Nav className="mr-auto">
                <Nav.Link as={NavLink} to="/student/dashboard">
                  <FontAwesomeIcon icon={faHome} className="me-1" /> Trang chủ
                </Nav.Link>

                <Nav.Link as={NavLink} to="/student/class-info">
                  <FontAwesomeIcon icon={faBuilding} className="me-1" /> Thông tin lớp học
                </Nav.Link>
                <Nav.Link as={NavLink} to="/student/schedule">
                  <FontAwesomeIcon icon={faCalendarAlt} className="me-1" /> Lịch học
                </Nav.Link>
                <NavDropdown title={<><FontAwesomeIcon icon={faBook} className="me-1" /> Môn học</>} id="course-dropdown">
                  <NavDropdown.Item as={NavLink} to="/student/course-registration">
                    <FontAwesomeIcon icon={faEdit} className="me-1" /> Đăng ký môn học
                  </NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/student/subjects">
                    <FontAwesomeIcon icon={faBookOpen} className="me-1" /> Môn đã học
                  </NavDropdown.Item>
                  <NavDropdown.Item as={NavLink} to="/student/scores">
                    <FontAwesomeIcon icon={faClipboardList} className="me-1" /> Xem điểm
                  </NavDropdown.Item>
                </NavDropdown>
                <Nav.Link as={NavLink} to="/forums">
                  <FontAwesomeIcon icon={faComments} className="me-1" />Diễn đàn
                </Nav.Link>
                {user && (
                  <Nav.Link as={NavLink} to="/chat" className="nav-link">
                    <FontAwesomeIcon icon={faMessage} className="me-1" /> Tin nhắn
                  </Nav.Link>
                )}
              </Nav>
            )}

            {/* Authentication Links */}
            <Nav className="ms-auto">
              {!user ? (
                <>
                  <Nav.Link as={Link} to="/login" className="btn btn-success me-2">
                    <FontAwesomeIcon icon={faSignInAlt} className="me-1" /> Đăng nhập
                  </Nav.Link>
                  <Nav.Link as={Link} to="/registerStudent" className="btn btn-outline-light">
                    <FontAwesomeIcon icon={faUserPlus} className="me-1" /> Đăng ký
                  </Nav.Link>
                </>
              ) : (
                <NavDropdown
                  title={
                    <div className="d-flex align-items-center">
                      <FontAwesomeIcon icon={faUserCircle} className="me-2" />
                      <span>{user.name}</span>
                    </div>
                  }
                  id="userDropdown"
                  align="end"
                >
                  <NavDropdown.Item as={Link} to="/profile">
                    <FontAwesomeIcon icon={faUser} className="me-2 fa-sm fa-fw" /> Thông tin cá nhân
                  </NavDropdown.Item>
                  <NavDropdown.Divider />
                  <NavDropdown.Item onClick={logout}>
                    <FontAwesomeIcon icon={faSignOutAlt} className="me-2 fa-sm fa-fw" /> Đăng xuất
                  </NavDropdown.Item>
                </NavDropdown>
              )}
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>
    </header>
  );
};

export default Header;