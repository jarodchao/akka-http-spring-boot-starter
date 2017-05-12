package c.j.framework.http;

import java.util.Optional;

public interface Authentication<T,R> {


    /**
     * 鉴权实现方法
     * @param t
     * @param <T>
     * @return
     */
    Optional<R> authorize(T t);

}
