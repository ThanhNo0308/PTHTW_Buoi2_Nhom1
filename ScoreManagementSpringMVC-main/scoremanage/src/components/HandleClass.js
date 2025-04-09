import React, { useContext, useEffect, useState } from 'react';
import Button from 'react-bootstrap/Button';
import Card from 'react-bootstrap/Card';
import { Link } from 'react-router-dom';
import { useSchoolYear } from '../reducers/SchoolYearContext';
import { MyUserContext } from '../App';
import Apis, { endpoints } from '../configs/Apis';
import cookie from "react-cookies";
import { UniqueSubjectTeacherIdContext } from '../reducers/UniqueSubjectTeacherIdContext';
const HandleClass = () => {
    const { selectedSchoolYearId } = useSchoolYear();
    const [user] = useContext(MyUserContext);
    const [subjects, setSubjects] = useState([]);
    const [uniqueSubjectTeacherIdList, setUniqueSubjectTeacherIdList] = useState([]);

    const {selectedSubjectTeacherId, setSelectedSubjectTeacherId } = useContext(UniqueSubjectTeacherIdContext);

    
    useEffect(() => {
        const fetchData = async () => {
            try {
                const requestData = {
                    'selectedSchoolYearId': selectedSchoolYearId,
                    'userUserName': user.username,
                };
                const headers = {
                    'Authorization': cookie.load('token'),
                    'Content-Type': 'application/json',
                };
                const response = await Apis.post(endpoints['listsubject'], requestData, { headers });

                if (response.status === 200) {
                    const responseData = response.data; // Lấy dữ liệu từ response
                    const uniqueSubjectTeacherIdList = responseData.uniqueSubjectTeacherIdList;
                    const listSubject = responseData.listSubject;
                    setSubjects(listSubject); // Đặt giá trị subjects
                    setUniqueSubjectTeacherIdList(uniqueSubjectTeacherIdList);;
                }
            } catch (error) {
                console.error('Error fetching data:', error);
            }
        };

        fetchData();
    });

    const handleGoToClass = (uniqueId) => {
        // Thực hiện xử lý khi nhấp vào nút "Đi đến môn học" với uniqueId được truyền vào.
        console.log(`Đã nhấp vào môn học với ID: ${uniqueId}`);
        
        // Đặt giá trị selectedSubjectTeacherId để truyền cho các thành phần khác
        setSelectedSubjectTeacherId(uniqueId);
    };


    const renderCards = (listSubject) => {
        return listSubject.map((subject, index) => (
            <Card key={index} id={`${uniqueSubjectTeacherIdList[index]}`} style={{ width: '18rem', margin: '10px 10px 10px 0' }}>
                <Card.Img variant="top" src="https://img.lovepik.com/photo/40006/4475.jpg_wh860.jpg" />
                <Card.Body>
                    <Card.Title>{subject.subjectName}</Card.Title>
                    <Card.Text>
                        Số tín chỉ: {subject.credits}
                    </Card.Text>
                    <Button
                        as={Link}
                        variant="secondary"
                        to="/liststudent"
                        onClick={() => handleGoToClass(uniqueSubjectTeacherIdList[index])}
                    >
                        Đi đến môn học
                    </Button>
                </Card.Body>
            </Card>
        ));
    };

    return (
        <>
            <h1>LỚP PHỤ TRÁCH</h1>
            <div style={{
                display: 'flex',
                flexDirection: 'row',
                flexWrap: 'wrap',
                border: '1px solid #ccc',
                padding: '10px',
            }}>
                {renderCards(subjects)}
            </div>
        </>
    );
};

export default HandleClass;