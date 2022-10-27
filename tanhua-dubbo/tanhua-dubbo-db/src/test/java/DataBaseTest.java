import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tanhua.dubbo.DubboDBApplication;
import com.tanhua.dubbo.mappers.UserMapper;
import com.tanhua.model.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DubboDBApplication.class)
public class DataBaseTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testFindByMobile() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>();
        User user = userMapper.selectOne(queryWrapper.eq(User::getMobile, "13800138000"));
        System.out.println(user);
    }

}
