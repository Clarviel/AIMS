package worker.aims.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import worker.aims.service.ex.*;
import worker.aims.service.itf.UserService;
import worker.aims.util.JsonResult;

@Slf4j
public class BaseController {
    //操作成功的状态码
    public static final int OK = 200;

    public static final int SHACK = 201;

    public static final int FAIL = 400;

    @Autowired
    private UserService userService;

    @ExceptionHandler(ServiceException.class)
    public JsonResult<Void> handleException(Throwable e){
        JsonResult<Void> result = new JsonResult<>(e);
        if (e instanceof NameDuplicateException){
            result.setState(4001);
            result.setMessage(e.getMessage());
        }else if (e instanceof InsertException){
            result.setState(4002);
            result.setMessage(e.getMessage());
        }else if (e instanceof PasswordNotMatchException){
            result.setState(4003);
            result.setMessage(e.getMessage());
        }else if(e instanceof NotFoundException){
            result.setState(4004);
            result.setMessage(e.getMessage());
        }else if(e instanceof UpdateException){
            result.setState(4005);
            result.setMessage(e.getMessage());
        }else if(e instanceof AccessDeniedException){
            result.setState(4006);
            result.setMessage(e.getMessage());
        }else if(e instanceof DeleteException){
            result.setState(4007);
            result.setMessage(e.getMessage());
        }
        return result;
    }

}
