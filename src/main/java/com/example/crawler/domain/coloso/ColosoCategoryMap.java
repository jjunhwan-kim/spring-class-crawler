package com.example.crawler.domain.coloso;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ColosoCategoryMap {
    CATEGORY001("드로잉", "인체드로잉", "드로잉", "취미드로잉"),
    CATEGORY002("드로잉", "캐릭터", "드로잉", "캐릭터일러스트"),
    CATEGORY003("드로잉", "디자인/컨셉아트", "드로잉", "컨셉 아트"),
    CATEGORY004("드로잉", "배경", "드로잉", "취미드로잉"),
    CATEGORY005("드로잉", "만화/웹툰", "드로잉", "웹툰/웹소설"),
    CATEGORY006("드로잉", "애니/3D", "드로잉", "캐릭터일러스트"),
    CATEGORY007("드로잉", "손그림/아이패드", "드로잉", "취미드로잉"),

    CATEGORY010("영상/3D", "영상편집", "영상/3D/애니메이션", "영상/사진"),
    CATEGORY011("영상/3D", "2D", "영상/3D/애니메이션", "모션그래픽"),
    CATEGORY012("영상/3D", "3D", "영상/3D/애니메이션", "3D/CG"),
    CATEGORY013("영상/3D", "VFX", "영상/3D/애니메이션", "3D/CG"),
    CATEGORY014("영상/3D", "블렌더", "영상/3D/애니메이션", "블렌더"),
    CATEGORY015("영상/3D", "애니메이션", "영상/3D/애니메이션", "애니메이션"),
    CATEGORY016("영상/3D", "릴레이", "영상/3D/애니메이션", "애니메이션"),

    CATEGORY020("게임제작", "캐릭터제작", "게임", "게임제작"),
    CATEGORY021("게임제작", "배경제작", "게임", "게임제작"),
    CATEGORY022("게임제작", "그래픽/디자인", "게임", "게임제작"),
    CATEGORY023("게임제작", "애니메이션", "게임", "게임제작"),
    CATEGORY024("게임제작", "FX", "게임", "게임제작"),
    CATEGORY025("게임제작", "게임기획", "게임", "게임제작"),
    CATEGORY026("게임제작", "게임프로그래밍", "게임", "게임개발"),

    CATEGORY030("베이킹/쿠킹", "제과", "요리", "베이킹/디저트"),
    CATEGORY031("베이킹/쿠킹", "제빵", "요리", "베이킹/디저트"),
    CATEGORY032("베이킹/쿠킹", "카페푸드", "요리", "베이킹/디저트"),
    CATEGORY033("베이킹/쿠킹", "요리", "요리", "요리/음료"),
    CATEGORY034("베이킹/쿠킹", "요식업", "요리", "요리/음료"),

    CATEGORY040("디자인", "그래픽디자인", "디자인", "2D/그래픽/브랜딩"),
    CATEGORY041("디자인", "브랜드디자인", "디자인", "2D/그래픽/브랜딩"),
    CATEGORY042("디자인", "UX/UI", "디자인", "UX/UI"),
    CATEGORY043("디자인", "웹/코딩", "프로그래밍", "웹"),
    CATEGORY044("디자인", "건축/인테리어", "디자인", "3D/건축"),
    CATEGORY045("디자인", "제품디자인", "디자인", "제품디자인"),

    CATEGORY050("AI/프로그래밍", "AI", "데이터사이언스", "인공지능"),
    CATEGORY051("AI/프로그래밍", "프로그래밍", "프로그래밍", "기타"),

    CATEGORY060("크리에이티브", "메타버스", "크리에이티브", "메타버스"),
    CATEGORY061("크리에이티브", "소설/글쓰기", "크리에이티브", "소설/글쓰기"),
    CATEGORY062("크리에이티브", "작곡", "크리에이티브", "음악"),
    CATEGORY063("크리에이티브", "패션/사진", "크리에이티브", "패션/사진"),

    CATEGORY070("헤어/뷰티", "헤어", "크리에이티브", "헤어/뷰티"),
    CATEGORY071("헤어/뷰티", "뷰티", "크리에이티브", "헤어/뷰티"),

    CATEGORY080("창업/재테크", "창업/쇼핑몰", "커리어", "창업/부업/재테크/쇼핑몰"),
    CATEGORY081("창업/재테크", "주식/연금", "금융/투자", "재테크/주식"),
    CATEGORY082("창업/재테크", "부동산", "금융/투자", "부동산"),

    CATEGORY090("커리어", "비즈니스", "커리어", "비즈니스"),
    CATEGORY091("커리어", "콘텐츠/마케팅", "커리어", "마케팅/기타"),
    CATEGORY092("커리어", "IT", "커리어", "기타");

    private final String originalMainCategory;
    private final String originalSubCategory;
    private final String convertedMainCategory;
    private final String convertedSubCategory;
}
