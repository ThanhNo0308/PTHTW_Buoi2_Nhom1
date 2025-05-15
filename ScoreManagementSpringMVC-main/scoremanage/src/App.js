import { BrowserRouter, Routes, Route } from "react-router-dom";
import Header from "./layout/Header";
import Footer from "./layout/Footer";
import Home from "./components/Home";
import Login from "./components/Login";
import React, { createContext, useReducer, useEffect } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import "./App.css";
import RegisterStudent from "./components/RegisterStudent";
import Profile from "./components/Profile";

import TeacherClassesList from "./components/TeacherClassesList";
import TeacherClassDetail from "./components/TeacherClassDetail";
import ScoreManagement from "./components/ScoreManagement";
import StudentDetail from "./components/StudentDetail";
import StudentScores from "./components/StudentScores";
import StudentSearch from "./components/StudentSearch";
import ScoreImport from "./components/ScoreImport";

import ForumList from "./components/ForumList";
import ForumDetail from "./components/ForumDetail";
import ForumCreate from "./components/ForumCreate";
import ForumEdit from "./components/ForumEdit";

import StudentScoresList from "./components/StudentScoresList";
import StudentClassInfo from "./components/StudentClassInfo";
import StudentSubjects from "./components/StudentSubjects";
import StudentCourseRegistration from './components/StudentCourseRegistration';

import MyUserReducer from "./reducers/MyUserReducer";
import { SchoolYearProvider } from "./reducers/SchoolYearContext";
import { UniqueSubjectTeacherIdProvider } from "./reducers/UniqueSubjectTeacherIdContext";
import cookie from "react-cookies";
import ChatPage from "./components/ChatPage";
import StudentDashboard from "./components/StudentDashboard";
import TeacherDashboard from "./components/TeacherDashboard";
export const MyUserContext = createContext();


const App = () => {
  const [user, dispatch] = useReducer(MyUserReducer, cookie.load("user") || null);
  return (
    <MyUserContext.Provider value={[user, dispatch]}>
      <SchoolYearProvider>
        <UniqueSubjectTeacherIdProvider>
          <BrowserRouter>
            <Header className="header" />
            <Routes className="routes">
              <Route path="/" element={<Home />} />
              <Route path="/registerstudent" element={<div style={{ margin: '50px 200px 50px 200px' }}><RegisterStudent /></div>} />
              <Route path="/login" element={<div style={{ margin: '100px' }}><Login /></div>} />
              <Route path="/profile" element={<div style={{ margin: '100px' }}><Profile /></div>} />

              <Route path="/student/dashboard" element={<div style={{ margin: '50px 150px' }}><StudentDashboard /></div>} />
              <Route path="/student/scores" element={<div style={{ margin: '50px 150px' }}><StudentScoresList /></div>} />
              <Route path="/student/class-info" element={<div style={{ margin: '50px 150px' }}><StudentClassInfo /></div>} />
              <Route path="/student/subjects" element={<div style={{ margin: '50px 150px' }}><StudentSubjects /></div>} />
              <Route path="/student/course-registration" element={<StudentCourseRegistration />} />

              <Route path="/teacher/dashboard" element={<div style={{ margin: '50px 150px' }}><TeacherDashboard /></div>} />
              <Route path="/teacher/classes" element={<div style={{ margin: '50px 150px' }}><TeacherClassesList /></div>} />
              <Route path="/teacher/classes/:classId" element={<div style={{ margin: '50px 150px' }}><TeacherClassDetail /></div>} />
              <Route path="/teacher/classes/:classId/scores" element={<div style={{ margin: '50px 150px' }}><ScoreManagement /></div>} />
              <Route path="/teacher/student/:studentCode/detail" element={<div style={{ margin: '50px 150px' }}><StudentDetail /></div>} />
              <Route path="/teacher/student/:studentCode/scores" element={<div style={{ margin: '50px 150px' }}><StudentScores /></div>} />
              <Route path="/teacher/students/search" element={<div style={{ margin: '50px 150px' }}><StudentSearch /></div>} />
              <Route path="/teacher/scores/import" element={<div style={{ margin: '50px 150px' }}><ScoreImport /></div>} />
              <Route path="/forums" element={<div style={{ margin: '50px 150px' }}><ForumList /></div>} />
              <Route path="/forums/create" element={<div style={{ margin: '50px 150px' }}><ForumCreate /></div>} />
              <Route path="/forums/:forumId" element={<div style={{ margin: '50px 150px' }}><ForumDetail /></div>} />
              <Route path="/forums/edit/:forumId" element={<ForumEdit />} />

              <Route path="/chat" element={<div style={{ margin: '50px 150px' }}><ChatPage /></div>} />

            </Routes>
            <Footer className="footer" />
          </BrowserRouter>
        </UniqueSubjectTeacherIdProvider>
      </SchoolYearProvider>
    </MyUserContext.Provider>
  );
}

export default App;
