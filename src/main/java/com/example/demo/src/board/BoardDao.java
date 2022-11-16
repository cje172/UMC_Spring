package com.example.demo.src.board;


import com.example.demo.src.board.model.GetBoardRes;
import com.example.demo.src.board.model.PatchBoardReq;
import com.example.demo.src.board.model.PostBoardReq;
import com.example.demo.src.user.model.GetUserRes;
import com.example.demo.src.user.model.PatchUserReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository //  [Persistence Layer에서 DAO를 명시하기 위해 사용]

/**
 * DAO란?
 * 데이터베이스 관련 작업을 전담하는 클래스
 * 데이터베이스에 연결하여, 입력 , 수정, 삭제, 조회 등의 작업을 수행
 */
public class BoardDao {

    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************

    private JdbcTemplate jdbcTemplate;

    @Autowired //readme 참고
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    // ******************************************************************************

    // 게시글 작성
    public int createPost(PostBoardReq postBoardReq) {
        String createPostQuery = "insert into Board (userIdx, title, content) VALUES (?,?,?)"; // 실행될 동적 쿼리문
        Object[] createPostParams = new Object[]{postBoardReq.getUserIdx(), postBoardReq.getTitle(), postBoardReq.getContent()}; // 동적 쿼리의 ?부분에 주입될 값
        this.jdbcTemplate.update(createPostQuery, createPostParams);
        // userIdx -> postBoardReq.getUserIdx(), title -> postBoardReq.getTitle(), content -> postBoardReq.getContent() 로 매핑(대응)시킨다음 쿼리문을 실행한다.
        // 즉 DB의 User Table에 (userIdx, title, content)값을 가지는 유저 데이터를 삽입(생성)한다.

        String lastInserIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInserIdQuery, int.class); // 해당 쿼리문의 결과 마지막으로 삽인된 유저의 userIdx번호를 반환한다.
    }

    // 게시글 수정
    public int modifyPost(PatchBoardReq patchBoardReq) {
        String modifyPostQuery = "update Board set title = ?, content = ? where boardIdx = ? "; // 해당 boardIdx를 만족하는 Board를 해당 title과 content로 변경한다.
        Object[] modifyPostParams = new Object[]{patchBoardReq.getTitle(), patchBoardReq.getContent(), patchBoardReq.getBoardIdx()}; // 주입될 값들(title, content, boardIdx) 순

        return this.jdbcTemplate.update(modifyPostQuery, modifyPostParams); // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0)
    }

    // Board 테이블에 존재하는 전체 게시글 정보 조회
    public List<GetBoardRes> getPosts() {
        String getPostQuery = "select * from Board"; //Board 테이블에 존재하는 모든 게시글 정보를 조회하는 쿼리
        return this.jdbcTemplate.query(getPostQuery,
                (rs, rowNum) -> new GetBoardRes(
                        rs.getInt("boardIdx"),
                        rs.getInt("userIdx"),
                        rs.getString("title"),
                        rs.getString("content")) // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
        ); // 복수개의 게시글들을 얻기 위해 jdbcTemplate 함수(Query, 객체 매핑 정보)의 결과 반환(동적쿼리가 아니므로 Parmas부분이 없음)
    }

    // 해당 title 갖는 게시글의 정보 조회
    public List<GetBoardRes> getPostsByTitle(String title) {
        String getPostsByTitleQuery = "select * from Board where title =?"; // 해당 제목을 만족하는 게시글을 조회하는 쿼리문
        String getPostsByTitleParams = title;
        return this.jdbcTemplate.query(getPostsByTitleQuery,
                (rs, rowNum) -> new GetBoardRes(
                        rs.getInt("boardIdx"),
                        rs.getInt("userIdx"),
                        rs.getString("title"),
                        rs.getString("content")), // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                getPostsByTitleParams); // 해당 title을 갖는 모든 게시글 정보를 얻기 위해 jdbcTemplate 함수(Query, 객체 매핑 정보, Params)의 결과 반환
    }

    // 해당 boardIdx 갖는 게시글 조회
    public GetBoardRes getPost(int boardIdx) {
        String getPostQuery = "select * from Board where boardIdx = ?"; // 해당 boardIdx를 만족하는 게시글을 조회하는 쿼리문
        int getUserParams = boardIdx;
        return this.jdbcTemplate.queryForObject(getPostQuery,
                (rs, rowNum) -> new GetBoardRes(
                        rs.getInt("boardIdx"),
                        rs.getInt("userIdx"),
                        rs.getString("title"),
                        rs.getString("content")), // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                getUserParams); // 한 개의 게시글을 얻기 위한 jdbcTemplate 함수(Query, 객체 매핑 정보, Params)의 결과 반환
    }
}