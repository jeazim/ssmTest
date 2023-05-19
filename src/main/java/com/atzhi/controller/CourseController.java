package com.atzhi.controller;

import com.atzhi.pojo.Course;
import com.atzhi.pojo.User;
import com.atzhi.service.CourseService;
import com.atzhi.service.SchoolService;
import com.atzhi.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@Controller
@RequestMapping("/course")
public class CourseController {
    @Autowired
    private CourseService courseService;
    @Autowired
    private Result result;
    @Autowired
    private SchoolService schoolService;

    //返回主页面
    @GetMapping("/main_page")
    public String main_page(HttpSession session)
    {
        //防止非法进入
        if(session.getAttribute("user")==null)
        {
            return "redirect:/user/login_page";
        }else{
            User user = (User) session.getAttribute("user");
            System.out.println(user);
            return "main";
        }
    }
    //新增课程页面
    @GetMapping("add_page")
    public String add_page(HttpSession session)
    {
        if(session.getAttribute("user")==null)
        {
            return "redirect:/user/login_page";
        }else{
            User user = (User) session.getAttribute("user");
            System.out.println(user);
            return "add";
        }
    }
    //修改课程页面
    @GetMapping("update_page")
    public String update_page(Integer id,HttpSession session)
    {
        session.removeAttribute("course");
        if(session.getAttribute("user")==null)
        {
            return "redirect:/user/login_page";
        }else{
            Course course=courseService.selectById(id);
            System.out.println(course);
            //System.out.println("1111111111111111111111111111111111111");
            session.setAttribute("course", course);
            return "update";
        }
    }
    //获取课程的信息
    @GetMapping("/get_course")
    @ResponseBody
    public Course get_course(HttpSession session)
    {
        Course course = (Course) session.getAttribute("course");
        System.out.println(session.getAttribute("course")+"--------------");
//        session.removeAttribute("course");
//        System.out.println(session.getAttribute("course")+"--------------");
        return course;
    }


//   ------------------------------------------------分割线---------------------------------------------------------------------
    //返回课程表数据
    @GetMapping("/main_solve")
    @ResponseBody
    public Result main_solve()
    {
        List<Course>courses=courseService.selectAll();
        List<Map<String, Object>> discourse = new ArrayList<>();
        for (Course course : courses) {
            Map<String, Object> courseMap = new HashMap<>();
            courseMap.put("id",course.getId());
            courseMap.put("image",course.getImage());
            courseMap.put("name",course.getName());
            courseMap.put("hours",course.getHours());
            courseMap.put("schools",schoolService.selectSchoolNameById(course.getSchools()).getSchoolName());
            discourse.add(courseMap);
        }
        System.out.println(discourse);
        result.setMsg("success");
        result.setCode(200);
        result.setData(discourse);
        return result;
    }

    @PostMapping("/select_solve")
    @ResponseBody
    public Result select_solve(@RequestBody Course course)
    {
        System.out.println("-----------------------------select_solve-------------------------------------");
        List<Course>courses=courseService.selectByCondition(course);
        List<Map<String, Object>> discourse = new ArrayList<>();
        for (Course res : courses) {
            Map<String, Object> courseMap = new HashMap<>();
            courseMap.put("id",res.getId());
            courseMap.put("image",res.getImage());
            courseMap.put("name",res.getName());
            courseMap.put("hours",res.getHours());
            courseMap.put("schools",schoolService.selectSchoolNameById(res.getSchools()).getSchoolName());
            discourse.add(courseMap);
        }
        System.out.println(discourse);
        result.setMsg("success");
        result.setCode(200);
        result.setData(discourse);
        return result;
    }

    //删除课程
    @PostMapping("/delete_solve")
    @ResponseBody
    public String delete_solve(@RequestParam("id") int id)
    {
        System.out.println(id);
        courseService.deleteById(id);
        return "success";
    }
//    /images/39c9aa77-9afe-4abc-8bf1-748dfc314a67.jpg
//    /image/6340cb07-4078-42d2-9f51-9c14c13d24dd.jpg
    //        6340cb07-4078-42d2-9f51-9c14c13d24dd
    //添加课程
    @PostMapping("/add_solve")
    @ResponseBody
    public Result add_solve(@RequestParam("imageFile") MultipartFile imageFile, @RequestParam("name") String name,
                            @RequestParam("hours") Integer hours, @RequestParam("schools") Integer schools)
    {
        List<Course> courses=courseService.selectAll();
        Boolean success =true;
        for(Course res :courses)
        {
            if(res.getName().equals(name))
            {
                success=false;
                break;
            }
        }
        if(success.equals(true)) {
            try {
                String imageName=null;
                //是否添加课程图片
                if (ObjectUtils.isEmpty(imageFile) || imageFile.getSize() <= 0) {
                    imageName = "ce8860e1-71e9-426e-8d0a-2b7b1bde9a24.jpg";//默认图片
                    System.out.println(imageName+"是null");
                }else{
                    // 判断上传的文件是否为图片类型
                    if (!imageFile.getContentType().startsWith("image/")) {
                        result.setMsg("imageFail");
                        return result;
                    }else{
                        imageName = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(imageFile.getOriginalFilename());
                        // 生成图片文件名
                        String imagePath = "D:\\develop\\JAVA\\javaproject\\ssmTest3\\src\\main\\webapp\\image\\" + imageName;
                        // 将图片保存到磁盘
                        Path imageFilePath = Paths.get(imagePath);
                        Files.write(imageFilePath, imageFile.getBytes());
                        System.out.println(imageName+"不是null");
                    }
                }
                Course course =new Course(null,imageName,name,hours,schools);
                //执行插入操作，按照升序产生id
                courseService.insertAutoId(course);
                System.out.println("{'module':'course save success'}");
                result.setMsg("success");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("{'module':'course save fail'}");
            result.setMsg("error");
        }
        result.setCode(200);
        return result;
    }
    //修改课程
    @PostMapping ("/update_solve")
    @ResponseBody
    public Result update_solve(@RequestParam("id") Integer id,@RequestParam("imageFile") MultipartFile imageFile,
                               @RequestParam("name") String name, @RequestParam("hours") Integer hours,
                               @RequestParam("schools") Integer schools
    ){
        System.out.println("---------------------------update------------------------------------");
        List<Course> courses=courseService.selectAll();
        Boolean success =true;
        for(Course res :courses)
        {
            if(!res.getId().equals(id)&&res.getName().equals(name))
            {
                success=false;
                break;
            }
        }
        if(success.equals(true)) {
            try {
                String imageName=courseService.selectImage(id);
                //是否修改课程图片
                if (!ObjectUtils.isEmpty(imageFile) && imageFile.getSize() > 0) {
                    // 判断上传的文件是否为图片类型
                    if (!imageFile.getContentType().startsWith("image/")) {
                        result.setMsg("imageFail");
                        result.setCode(200);
                        return result;
                    }else{
                        imageName = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(imageFile.getOriginalFilename());
                        // 生成图片文件名
                        String imagePath = "D:\\develop\\JAVA\\javaproject\\ssmTest3\\src\\main\\webapp\\image\\" + imageName;
                        // 将图片保存到磁盘
                        Path imageFilePath = Paths.get(imagePath);
                        Files.write(imageFilePath, imageFile.getBytes());
                        System.out.println(imageName+"不是null");
                    }
                }
                Course course =new Course(id,imageName,name,hours,schools);
                System.out.println(course+"--------------------update");
                courseService.update(course);
                System.out.println(course+"{'module':'course update success'}");
                result.setMsg("success");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            result.setMsg("nameFail");
            System.out.println("{'module':'course update fail'}");
        }
        result.setCode(200);

        return result;
    }
}
//b4ab724d-7806-4dfd-9d0c-88b7f579c198.png
