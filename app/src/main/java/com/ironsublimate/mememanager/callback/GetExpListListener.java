package com.ironsublimate.mememanager.callback;

import com.ironsublimate.mememanager.bean.Expression;

import java.util.List;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/07/12
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public interface GetExpListListener {
    public void onFinish(List<Expression> expressions);
}
