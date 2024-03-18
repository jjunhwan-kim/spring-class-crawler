package com.example.crawler.domain.coloso;

import com.example.crawler.domain.CategoryMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ColosoCategoryMap implements CategoryMap {
    CATEGORY001("드로잉", "인체드로잉", "드로잉", "취미드로잉"),
    CATEGORY002("드로잉", "캐릭터", "드로잉", "캐릭터일러스트"),
    CATEGORY003("드로잉", "디자인/컨셉아트", "드로잉", "컨셉 아트"),
    CATEGORY004("드로잉", "배경", "드로잉", "취미드로잉"),
    CATEGORY005("드로잉", "만화/웹툰", "드로잉", "웹툰/웹소설"),
    CATEGORY006("드로잉", "애니/3D", "드로잉", "캐릭터일러스트"),
    CATEGORY007("드로잉", "손그림/아이패드", "드로잉", "취미드로잉"),
    CATEGORY008("영상/3D", "영상편집", "영상/3D/애니메이션", "영상/사진"),
    CATEGORY009("영상/3D", "2D", "영상/3D/애니메이션", "모션그래픽"),
    CATEGORY010("영상/3D", "3D", "영상/3D/애니메이션", "3D/CG"),
    CATEGORY011("영상/3D", "VFX", "영상/3D/애니메이션", "3D/CG"),
    CATEGORY012("영상/3D", "블렌더", "영상/3D/애니메이션", "블렌더"),
    CATEGORY013("영상/3D", "애니메이션", "영상/3D/애니메이션", "애니메이션"),
    CATEGORY014("영상/3D", "릴레이", "영상/3D/애니메이션", "애니메이션"),
    CATEGORY015("게임제작", "캐릭터제작", "게임", "게임제작"),
    CATEGORY016("게임제작", "배경제작", "게임", "게임제작"),
    CATEGORY017("게임제작", "그래픽/디자인", "게임", "게임제작"),
    CATEGORY018("게임제작", "애니메이션", "게임", "게임제작"),
    CATEGORY019("게임제작", "FX", "게임", "게임제작"),
    CATEGORY020("게임제작", "게임기획", "게임", "게임제작"),
    CATEGORY021("게임제작", "게임프로그래밍", "게임", "게임개발"),
    CATEGORY022("베이킹/쿠킹", "제과", "요리", "베이킹/디저트"),
    CATEGORY023("베이킹/쿠킹", "제빵", "요리", "베이킹/디저트"),
    CATEGORY024("베이킹/쿠킹", "카페푸드", "요리", "베이킹/디저트"),
    CATEGORY025("베이킹/쿠킹", "요리", "요리", "요리/음료"),
    CATEGORY026("베이킹/쿠킹", "요식업", "요리", "요리/음료"),
    CATEGORY027("디자인", "그래픽디자인", "디자인", "2D/그래픽/브랜딩"),
    CATEGORY028("디자인", "브랜드디자인", "디자인", "2D/그래픽/브랜딩"),
    CATEGORY029("디자인", "UX/UI", "디자인", "UX/UI"),
    CATEGORY030("디자인", "웹/코딩", "프로그래밍", "웹"),
    CATEGORY031("디자인", "건축/인테리어", "디자인", "3D/건축"),
    CATEGORY032("디자인", "제품디자인", "디자인", "제품디자인"),
    CATEGORY033("AI/프로그래밍", "AI", "데이터사이언스", "인공지능"),
    CATEGORY034("AI/프로그래밍", "프로그래밍", "데이터사이언스", "인공지능"),
    CATEGORY035("크리에이티브", "메타버스", "크리에이티브", "메타버스"),
    CATEGORY036("크리에이티브", "소설/글쓰기", "크리에이티브", "소설/글쓰기"),
    CATEGORY037("크리에이티브", "작곡", "크리에이티브", "음악"),
    CATEGORY038("크리에이티브", "패션/사진", "크리에이티브", "패션/사진"),
    CATEGORY039("헤어/뷰티", "헤어", "크리에이티브", "헤어/뷰티"),
    CATEGORY040("헤어/뷰티", "뷰티", "크리에이티브", "헤어/뷰티"),
    CATEGORY041("창업/재테크", "창업/쇼핑몰", "커리어", "창업/부업/재테크/쇼핑몰"),
    CATEGORY042("창업/재테크", "주식/연금", "금융/투자", "재테크/주식"),
    CATEGORY043("창업/재테크", "부동산", "금융/투자", "부동산"),
    CATEGORY044("커리어", "비즈니스", "커리어", "비즈니스"),
    CATEGORY045("커리어", "콘텐츠/마케팅", "커리어", "마케팅/기타"),
    CATEGORY046("커리어", "IT", "커리어", "기타");

    private final String originalMainCategory;
    private final String originalSubCategory;
    private final String convertedMainCategory;
    private final String convertedSubCategory;
}
