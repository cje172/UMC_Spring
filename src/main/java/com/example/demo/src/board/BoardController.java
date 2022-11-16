package com.example.demo.src.board;

import com.example.demo.src.board.model.*;
import com.example.demo.src.user.model.GetUserRes;
import com.example.demo.src.user.model.PatchUserReq;
import com.example.demo.src.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import static com.example.demo.config.BaseResponseStatus.*;

@RestController // Rest API 또는 WebAPI를 개발하기 위한 어노테이션. @Controller + @ResponseBody 를 합친것.
// @Controller      [Presentation Layer에서 Contoller를 명시하기 위해 사용]
//  [Presentation Layer?] 클라이언트와 최초로 만나는 곳으로 데이터 입출력이 발생하는 곳
//  Web MVC 코드에 사용되는 어노테이션. @RequestMapping 어노테이션을 해당 어노테이션 밑에서만 사용할 수 있다.
// @ResponseBody    모든 method의 return object를 적절한 형태로 변환 후, HTTP Response Body에 담아 반환.
@RequestMapping("/app/boards")
// method가 어떤 HTTP 요청을 처리할 것인가를 작성한다.
// 요청에 대해 어떤 Controller, 어떤 메소드가 처리할지를 맵핑하기 위한 어노테이션
// URL(/app/users)을 컨트롤러의 메서드와 매핑할 때 사용
/**
 * Controller란?
 * 사용자의 Request를 전달받아 요청의 처리를 담당하는 Service, Prodiver 를 호출
 */
public class BoardController {
    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************

    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log를 남기기: 일단은 모르고 넘어가셔도 무방합니다.

    @Autowired  // 객체 생성을 스프링에서 자동으로 생성해주는 역할. 주입하려 하는 객체의 타입이 일치하는 객체를 자동으로 주입한다.
    // IoC(Inversion of Control, 제어의 역전) / DI(Dependency Injection, 의존관계 주입)에 대한 공부하시면, 더 깊이 있게 Spring에 대한 공부를 하실 수 있을 겁니다!(일단은 모르고 넘어가셔도 무방합니다.)
    // IoC 간단설명,  메소드나 객체의 호출작업을 개발자가 결정하는 것이 아니라, 외부에서 결정되는 것을 의미
    // DI 간단설명, 객체를 직접 생성하는 게 아니라 외부에서 생성한 후 주입 시켜주는 방식
    private final BoardProvider boardProvider;
    @Autowired
    private final BoardService boardService;
    @Autowired
    private final JwtService jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!


    public BoardController(BoardProvider boardProvider, BoardService boardService, JwtService jwtService) {
        this.boardProvider = boardProvider;
        this.boardService = boardService;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!
    }

    // ******************************************************************************

    /**
     * 게시글 작성 API
     * [POST] /boards
     */
    // Body
    @ResponseBody
    @PostMapping("")    // POST 방식의 요청을 매핑하기 위한 어노테이션
    public BaseResponse<PostBoardRes> createPost(@RequestBody PostBoardReq postBoardReq) {
        //  @RequestBody란, 클라이언트가 전송하는 HTTP Request Body(우리는 JSON으로 통신하니, 이 경우 body는 JSON)를 자바 객체로 매핑시켜주는 어노테이션
        // email에 값이 존재하는지, 빈 값으로 요청하지는 않았는지 검사합니다. 빈값으로 요청했다면 에러 메시지를 보냅니다.
        if (postBoardReq.getTitle() == null) {
            return new BaseResponse<>(POST_BOARD_EMPTY_TITLE);
        }
        if (postBoardReq.getContent() == null) {
            return new BaseResponse<>(POST_BOARD_EMPTY_CONTENT);
        }
        try {
            PostBoardRes postBoardRes = boardService.createPost(postBoardReq);
            return new BaseResponse<>(postBoardRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 모든 게시글 조회 API
     * [GET] /boards
     *
     * 또는
     *
     * 해당 제목을 같는 게시글의 정보 조회 API
     * [GET] /boards? title=
     */
    //Query String
    @ResponseBody   // return되는 자바 객체를 JSON으로 바꿔서 HTTP body에 담는 어노테이션.
    //  JSON은 HTTP 통신 시, 데이터를 주고받을 때 많이 쓰이는 데이터 포맷.
    @GetMapping("") // (GET) 127.0.0.1:9000/app/boards
    // GET 방식의 요청을 매핑하기 위한 어노테이션
    public BaseResponse<List<GetBoardRes>> getPosts(@RequestParam(required = false) String title) {
        //  @RequestParam은, 1개의 HTTP Request 파라미터를 받을 수 있는 어노테이션(?뒤의 값). default로 RequestParam은 반드시 값이 존재해야 하도록 설정되어 있지만, (전송 안되면 400 Error 유발)
        //  지금 예시와 같이 required 설정으로 필수 값에서 제외 시킬 수 있음
        //  defaultValue를 통해, 기본값(파라미터가 없는 경우, 해당 파라미터의 기본값 설정)을 지정할 수 있음
        try {
            if (title == null) { // query string인 title 없을 경우, 그냥 전체 유저정보를 불러온다.
                List<GetBoardRes> getBoardRes = boardProvider.getPosts();
                return new BaseResponse<>(getBoardRes);
            }
            // query string인 title이 있을 경우, 조건을 만족하는 게시글을 불러온다.
            List<GetBoardRes> getBoardRes = boardProvider.getPostsByTitle(title);
            return new BaseResponse<>(getBoardRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 게시글 1개 조회 API
     * [GET] /boards/:boardIdx
     */
    // Path-variable
    @ResponseBody
    @GetMapping("/{boardIdx}") // (GET) 127.0.0.1:9000/app/boards/:boardIdx
    public BaseResponse<GetBoardRes> getPost(@PathVariable("boardIdx") int boardIdx) {
        // @PathVariable RESTful(URL)에서 명시된 파라미터({})를 받는 어노테이션, 이 경우 boardIdx값을 받아옴.
        //  null값 or 공백값이 들어가는 경우는 적용하지 말 것
        //  .(dot)이 포함된 경우, .을 포함한 그 뒤가 잘려서 들어감
        // Get Posts
        try {
            GetBoardRes getBoardRes = boardProvider.getPost(boardIdx);
            return new BaseResponse<>(getBoardRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }

    }

    /**
     * 게시글 수정 API
     * [PATCH] /boards/:boardIdx
     */
    @ResponseBody
    @PatchMapping("/{boardIdx}")
    public BaseResponse<String> modifyPost(@PathVariable("boardIdx") int boardIdx, @RequestBody Board board) {
        try {
/**
 *********** 해당 부분은 7주차 - JWT 수업 후 주석해체 해주세요!  ****************
         //jwt에서 idx 추출.
         int userIdxByJwt = jwtService.getUserIdx();
         //userIdx와 접근한 유저가 같은지 확인
         if(userIdx != userIdxByJwt){
             return new BaseResponse<>(INVALID_USER_JWT);
             }
         //같다면 유저네임 변경
 **************************************************************************
 */
            PatchBoardReq patchBoardReq = new PatchBoardReq(boardIdx, board.getTitle(), board.getContent());
            boardService.modifyPost(patchBoardReq);

            String result = "게시글이 수정되었습니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
