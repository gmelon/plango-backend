package dev.gmelon.plango.exception;

public class ErrorMessages {

    private ErrorMessages() {
    }

    public static String INTERNAL_SERVER_ERROR_MESSAGE = "서버 오류가 발생했습니다. 문제가 계속될 경우 관리자에게 문의해주세요.";
    public static String NOT_FOUND_ERROR_MESSAGE = "존재하지 않는 자원입니다.";
    public static String UNAUTHORIZED_ERROR_MESSAGE = "로그인이 필요합니다.";
    public static String LOGIN_FAILURE_ERROR_MESSAGE = "아이디 또는 비밀번호가 올바르지 않습니다.";

}
