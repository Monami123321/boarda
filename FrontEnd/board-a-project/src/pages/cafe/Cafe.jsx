import React, { useState, useEffect } from "react";
import { Link, Route, useParams } from "react-router-dom";
import axios from "axios";

const { kakao } = window;
import { useRecoilState } from "recoil";
import { cafeListState } from "../../recoil/atoms/cafeState";

import { getCafeList } from "../../api/cafeAPI";

// 마커 이미지 주소(추후 디자인 변경 필요)
const imageSrc =
  "https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png";

export default function Cafe() {
  const [Location, setLocation] = useState("");
  const [Brand, setBrand] = useState("");
  const [positions, setCafeList] = useRecoilState(cafeListState);

  useEffect(() => {
    // 컴포넌트가 처음 마운트될 때 초기화
    setCafeList([]);
  }, []);

  const onSubmitHandler = async (event) => {
    event.preventDefault();

    const temp = await getCafeList(Location, Brand);
    // 백에 filter로 해당하는 매장 LAT_LNG 값들 리스트 가져온다. 일단은 demo용 데이터 position 사용
    setCafeList(temp);
  };

  const renderContent = () => {
    useEffect(() => {
      const container = document.getElementById("map");
      const options = {
        center: new kakao.maps.LatLng(37.566826, 126.9786567),
        level: 9,
      };
      const map = new kakao.maps.Map(container, options);

      if (positions.length > 0) {
        const map = new kakao.maps.Map(container, options);
        let bounds = new kakao.maps.LatLngBounds();
        const imageSize = new window.kakao.maps.Size(24, 35);
        const markerImage = new window.kakao.maps.MarkerImage(
          imageSrc,
          imageSize
        );

        for (let i = 0; i < positions.length; i++) {
          const temp_point = new kakao.maps.LatLng(
            positions[i].latitude,
            positions[i].longitude
          );
          const marker = new window.kakao.maps.Marker({
            map: map,
            position: temp_point,
            title: positions[i].title,
            image: markerImage,
          });

          bounds.extend(temp_point);
        }

        map.setBounds(bounds);
      }
    }, [positions]);

    return (
      <>
        <div id="map" style={{ width: "500px", height: "400px" }}></div>
        <ul>
          {!!positions &&
            positions.map((data, idx) => (
              <li key={idx}>
                <div style={{border: "1px solid black"}}>
                  <div>
                    {data.brand} {data.branch}
                  </div>
                  <div> 위치: {data.location} </div>
                  <div> 연락처: {data.contact} </div>
                  <div> 평점: {data.rate} </div>
                </div>
              </li>
            ))}
        </ul>
      </>
    );
  };

  return (
    <div>
      <div>
        <form onSubmit={onSubmitHandler}>
          <label>
            지역 :
            <select
              value={Location}
              onChange={(e) => setLocation(e.target.value)}
            >
              <option value="">상관없음</option>
              <option value="강남구">강남구</option>
              <option value="마포구">마포구</option>
            </select>
          </label>
          <label>
            브랜드 :
            <select value={Brand} onChange={(e) => setBrand(e.target.value)}>
              <option value="">상관없음</option>
              <option value="레드버튼">레드버튼</option>
              <option value="홈즈앤루팡">홈즈앤 루팡</option>
              <option value="히어로">히어로 보드게임카페</option>
            </select>
          </label>
          <button>검색</button>
        </form>
      </div>
      <div>{renderContent()}</div>
    </div>
  );
}
