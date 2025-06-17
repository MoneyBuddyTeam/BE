import { useParams, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';

interface AdvisorSummary {
  id: number;
  name: string;
  price: number;
  isOnline: boolean;
}

interface AdvisorDetail {
  id: number;
  name: string;
  bio: string;
  isOnline: boolean;
  available: boolean;
  price: number;
  certificationFile: string;
  userId: number;
  recommendedAdvisors: AdvisorSummary[];
}

interface Props {
  token: string;
  clientId: number | null;
  onRoomCreated: (roomId: number) => void;
}

function AdvisorDetailPage({ token, clientId, onRoomCreated }: Props) {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [advisor, setAdvisor] = useState<AdvisorDetail | null>(null);

  useEffect(() => {
    if (!id || !token) return;

    fetch(`http://localhost:8080/api/v1/advisors/${id}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then((data) => setAdvisor(data))
      .catch((err) => console.error('전문가 정보 로딩 실패:', err));
  }, [id, token]);

const handleConsultationClick = async () => {
  if (!advisor || !clientId || !token) return;

  try {
    const response = await fetch(`http://localhost:8080/api/v1/consultation/rooms`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        consultantId: advisor.userId,
        topic: '상담 요청', // 또는 advisor.topic 등
        durationMinutes: 30,
        amount: advisor.price,
        paymentMethod: 'CARD', // 또는 테스트용 'FREE'
      }),
    });

    if (!response.ok) throw new Error('채팅방 생성 실패');

    const roomId = await response.json();
    onRoomCreated(roomId);
    navigate(`/consultation/rooms/${roomId}`);
  } catch (err) {
    console.error('채팅방 생성 중 오류:', err);
    alert('채팅방을 생성할 수 없습니다. 다시 시도해 주세요.');
  }
};


  if (!advisor) return <p>로딩 중...</p>;

  return (
    <div className="advisor-detail-container">
      <h1>{advisor.name}</h1>
      <p>{advisor.bio}</p>

      <div>
        <p>💬 온라인 상태: {advisor.isOnline ? '온라인' : '오프라인'}</p>
        <p>✅ 상담 가능 여부: {advisor.available ? '가능' : '불가'}</p>
        <p>💰 상담 가격: {advisor.price.toLocaleString()}원</p>
      </div>

      {advisor.certificationFile && (
        <div>
          <p>
            📝 인증서:{' '}
            <a href={advisor.certificationFile} target="_blank" rel="noopener noreferrer">
              보기
            </a>
          </p>
        </div>
      )}

      <button
        onClick={handleConsultationClick}
        disabled={!advisor.available}
        className="bg-blue-500 text-white px-4 py-2 mt-4 rounded"
      >
        바로 상담 시작하기
      </button>

      <div className="recommendation-section mt-6">
        <h2>추천 전문가</h2>
        <ul>
          {advisor.recommendedAdvisors.map((rec: AdvisorSummary) => (
            <li
              key={rec.id}
              className="cursor-pointer hover:underline"
              onClick={() => navigate(`/advisors/${rec.id}`)}
            >
              <strong>{rec.name}</strong> - {rec.price.toLocaleString()}원 /{' '}
              {rec.isOnline ? '온라인' : '오프라인'}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

export default AdvisorDetailPage;
