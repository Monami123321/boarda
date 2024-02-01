import React, { useEffect, useState } from 'react';
import { useRecoilState } from 'recoil';
import { getMoimList, checkRoom } from '../../api/moimAPI';
import { moimListState, locationState } from '../../recoil/atoms/moimState';
import { useLocation } from 'react-router-dom';
import styled from 'styled-components';
import Modal from "react-modal";
import MoimDetailModal from './MoimDetailModal';
import MoimMakeModal from './MoimMakeModal';
import Pagination from '@material-ui/lab/Pagination';


Modal.setAppElement("#root");

const StyledButton = styled.button`
  padding: 10px 20px;
  border-radius: 5px;
  border: none;
  color: white;
  background-color: #3498db;

  &:hover {
    background-color: #2980b9;
  }
`;

const MoimList = () => {
  const [moimList, setMoimList] = useRecoilState(moimListState);
  const [location, setLocation] = useRecoilState(locationState);
  const [sort, setSort] = useState('1');
  const [modalIsOpen, setModalIsOpen] = useState(false);
  const location2 = useLocation();

  const [detailModalIsOpen, setDetailModalIsOpen] = useState(false);
  const [selectedMoimId, setSelectedMoimId] = useState(null);

  const [makeModalIsOpen, setMakeModalIsOpen] = useState(false);

  const [totalItemsCount, setTotalItemsCount] = useState(0);
  const [activePage, setActivePage] = useState(1);
  const itemsCountPerPage = 2;

  const getMoimListData = async () => {
    const data = await getMoimList(location, sort);
    setMoimList(data);
  };

  const handleLocationChange = (selectedLocation) => {
    setLocation(selectedLocation); // 햄버거 메뉴에서 선택한 값으로 location 업데이트
  };

  const handleSortChange = (selectedSort) => {
    setSort(selectedSort); // 정렬 방식 선택 시 sort 상태 업데이트
  };

  const openDetailModal = (moimId) => {
    if (!detailModalIsOpen) {
      setSelectedMoimId(moimId);
      setDetailModalIsOpen(true);
    } else {
      console.log('Modal is already open');
    }
  };

  const closeDetailModal = () => {
    setDetailModalIsOpen(false);
  };

  const openMakeModal = async () => {
    const data = await checkRoom(11);
    console.log("ㅇㅇ" + data);
    if (data === 0) {
      setMakeModalIsOpen(true);
    } else if (data === 1) {
      setModalIsOpen(true);
    }
  };


  const closeMakeModal = () => {
    setMakeModalIsOpen(false);
  };

  const handlePageChange = async (event, pageNumber) => {
    setActivePage(pageNumber);
  };
  // const handlePageChange = (pageNumber) => {
  //   setActivePage(pageNumber);
  // };



  const currentMoimList = moimList.slice((activePage - 1) * itemsCountPerPage, activePage * itemsCountPerPage);

  

  useEffect(() => {
    getMoimListData();
    if (location2.state?.updated){
      getMoimListData(); 
    }
  }, [location2, location, sort]); // location 값이 변경될 때마다 API 요청

  useEffect(() => {
    setTotalItemsCount(moimList.length);
  }, [moimList]);

  return (
    <div>
      <label>
        Location:
        <select value={location} onChange={(e) => handleLocationChange(e.target.value)}>
          <option value="서울시 강남구">강남구</option>
          <option value="서울시 마포구">마포구</option>
        </select>
      </label>
      < br/>
      <label>
        정렬:
        <select value={sort} onChange={(e) => handleSortChange(e.target.value)}>
          <option value="1">최신순</option>
          <option value="2">마감임박순</option>
          <option value="3">모집일시</option>
        </select>
      </label>
      
      <ul style={{ listStyleType: 'none' }} >
        {currentMoimList.map((moim) => (
          <li key={moim.id}>
          <button onClick={() => openDetailModal(moim.id)}>
            {moim.id} {moim.title} {moim.datetime} {moim.currentNumber}/{moim.number}
          </button>
        </li>
        ))}
      </ul>

      {selectedMoimId && (
        <MoimDetailModal
          moimId={selectedMoimId}
          isOpen={detailModalIsOpen}
          onRequestClose={closeDetailModal}
        />
      )}


        {/* <Pagination
          activePage={activePage}
          itemsCountPerPage={itemsCountPerPage}
          totalItemsCount={totalItemsCount} // 전체 아이템의 수, API 응답에서 가져올 수 있음
          pageRangeDisplayed={5} // 한 번에 보여줄 페이지 번호의 수
          onChange={handlePageChange}
        /> */}
      <Pagination
        count={Math.ceil(totalItemsCount / itemsCountPerPage)}
        page={activePage}
        onChange={handlePageChange}
      />
     
     


      <div>
        <Modal
          isOpen={modalIsOpen}
          onRequestClose={() => setModalIsOpen(false)}
          style={{
            overlay: {
              backgroundColor: 'rgba(0, 0, 0, 0.5)'
            },
            content: {
              color: 'lightsteelblue'
            }
          }}
        >
          <h2>알림</h2>
          <p>이미 참여 중인 모임이 있습니다!</p>
          <StyledButton onClick={() => setModalIsOpen(false)}>확인</StyledButton>
        </Modal>
        <StyledButton onClick={() => openMakeModal()}>글쓰기</StyledButton>
        <MoimMakeModal
        isOpen={makeModalIsOpen}
        onRequestClose={closeMakeModal}
        />
      </div>
    </div>
  );
};

export default MoimList;