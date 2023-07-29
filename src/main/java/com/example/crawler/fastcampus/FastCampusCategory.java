package com.example.crawler.fastcampus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FastCampusCategory {

    CATEGORY001("프로그래밍", "프론트엔드 개발", "https://fastcampus.co.kr/category_online_programmingfront"),
    CATEGORY002("프로그래밍", "백엔드 개발", "https://fastcampus.co.kr/category_online_programmingback"),
    CATEGORY003("프로그래밍", "모바일 앱 개발", "https://fastcampus.co.kr/category_online_programmingapp"),
    CATEGORY004("프로그래밍", "DevOps/Infra", "https://fastcampus.co.kr/category_online_programmingdevops"),
    CATEGORY005("프로그래밍", "블록체인 개발", "https://fastcampus.co.kr/category_online_programmingblockchain"),
    CATEGORY006("프로그래밍", "게임 개발", "https://fastcampus.co.kr/category_online_programminggame"),
    CATEGORY007("프로그래밍", "컴퓨터 공학/SW 엔지니어링", "https://fastcampus.co.kr/category_online_programmingcareer"),
    CATEGORY008("데이터사이언스", "데이터분석", "https://fastcampus.co.kr/category_online_datascienceanal"),
    CATEGORY009("데이터사이언스", "머신러닝", "https://fastcampus.co.kr/category_online_datasciencemr"),
    CATEGORY010("데이터사이언스", "데이터엔지니어링", "https://fastcampus.co.kr/category_online_datascienceeng"),
    CATEGORY011("인공지능", "딥러닝", "https://fastcampus.co.kr/category_online_datasciencedeep"),
    CATEGORY012("인공지능", "컴퓨터비전", "https://fastcampus.co.kr/category_online_datasciencecv"),
    CATEGORY013("인공지능", "자연어처리", "https://fastcampus.co.kr/category_online_datasciencenlp"),
    //    CATEGORY014("인공지능","ChatGPT","https://fastcampus.co.kr/impact_all_chatgpt"),
    //    CATEGORY015("디자인","PLUS X SHARE X","https://fastcampus.co.kr/category_online_plusx"),
    CATEGORY016("디자인", "UX/UI", "https://fastcampus.co.kr/category_online_dgnuxui"),
    CATEGORY017("디자인", "2D/그래픽/브랜딩", "https://fastcampus.co.kr/category_online_dgnvisual"),
    CATEGORY018("디자인", "3D/건축", "https://fastcampus.co.kr/category_online_dgn3d"),
    CATEGORY019("영상/3D", "영상/사진", "https://fastcampus.co.kr/category_online_videoedit"),
    CATEGORY020("영상/3D", "모션그래픽", "https://fastcampus.co.kr/category_online_videomotion"),
    CATEGORY021("영상/3D", "블렌더", "https://fastcampus.co.kr/category_online_videogame"),
    CATEGORY022("영상/3D", "3D·CG", "https://fastcampus.co.kr/category_online_videovfx"),
    //    CATEGORY023("일러스트","네오아카데미","https://fastcampus.co.kr/category_online_neo"),
    CATEGORY024("일러스트", "캐릭터일러스트", "https://fastcampus.co.kr/category_online_illustcha"),
    CATEGORY025("일러스트", "웹툰/웹소설", "https://fastcampus.co.kr/category_online_illustweb"),
    CATEGORY026("일러스트", "취미드로잉", "https://fastcampus.co.kr/category_online_illusthobby"),
    //    CATEGORY027("금융/투자","권오상 금융 아카데미","https://fastcampus.co.kr/category_online_kfa"),
    CATEGORY028("금융/투자", "부동산", "https://fastcampus.co.kr/category_online_financerealestate"),
    CATEGORY029("금융/투자", "금융 투자 실무", "https://fastcampus.co.kr/category_online_financeinvest"),
    CATEGORY030("금융/투자", "재무/회계/세무", "https://fastcampus.co.kr/category_online_financeaccounting"),
    CATEGORY031("금융/투자", "재테크/주식", "https://fastcampus.co.kr/category_online_invest"),
    CATEGORY032("마케팅", "디지털마케팅", "https://fastcampus.co.kr/category_online_marketingdgt"),
    CATEGORY033("마케팅", "콘텐츠/마케팅 전략", "https://fastcampus.co.kr/category_online_marketingsns"),
    CATEGORY034("업무 생산성", "엑셀/VBA/업무자동화", "https://fastcampus.co.kr/category_online_bizexcel"),
    CATEGORY035("업무 생산성", "PPT/보고서", "https://fastcampus.co.kr/category_online_bizppt"),
    CATEGORY036("업무 생산성", "글쓰기/카피라이팅", "https://fastcampus.co.kr/category_online_marketingwriting"),
    //    CATEGORY037("업무 생산성","숏북","https://fastcampus.co.kr/shortbook"),
    CATEGORY038("업무 생산성", "ChatGPT 활용", "https://fastcampus.co.kr/category_online_bizchatgpt"),
    CATEGORY039("비즈니스/기획", "PM / PO / 기획", "https://fastcampus.co.kr/category_online_bizcom"),
    CATEGORY040("비즈니스/기획", "경영/부업/창업", "https://fastcampus.co.kr/category_online_entrepreneurship"),
    CATEGORY041("비즈니스/기획", "커뮤니케이션/리더십", "https://fastcampus.co.kr/category_online_bizleadership");

    private final String mainCategory;
    private final String subCategory;
    private final String url;
}
