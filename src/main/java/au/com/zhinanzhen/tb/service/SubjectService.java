package au.com.zhinanzhen.tb.service;

import java.util.List;

import au.com.zhinanzhen.tb.service.pojo.SubjectResultDTO;



public interface SubjectService {
    /**
     *根据区域和类目获取课程
     * @param categoryId 类目编号  传0 则不限类目
     * @param regionId   区域Id   传0 则不限区域
     * @return  课程集合
     * @throws ServiceException
     */
   List<SubjectResultDTO> getSubjectList(int categoryId,int regionId,String classify) throws ServiceException ;
   /**
    * 根据编号获取课程详情
    * @param id
    * @param regionId
    * @return
    * @throws ServiceException
    */
   SubjectResultDTO getSubjectById(int id,int regionId) throws ServiceException;
   
   /**
    * 根据日期修改课程状态
    * @return
    * @throws ServiceException
    */
   boolean updateSunjectStateByDate() throws ServiceException;
   /**
    * 获取课程购买数量
    * @return
    * @throws ServiceException
    */
   int getSubjectCount(int subjectId) throws ServiceException;
   /**
    * 获取课程开课数量
    * @param id
    * @return
    * @throws ServiceException
    */
   int getSubjectMinCount(int subjectId) throws ServiceException;
   /**
    * 获取课程最低价格
    * @param subjectId
    * @return
    * @throws ServiceException
    */
   double getSubjectMinMoney(int subjectId,int regionId) throws ServiceException;
   /**
    * 获取课程当前价格
    * @param subjectId
    * @return
    * @throws ServiceException
    */
   double getSubjectNowMoney(int subjectId,int regionId) throws ServiceException;
   /**
    * 获取下一位报名课程价格
    * @param subjectId
    * @return
    * @throws ServiceException
    */
   double getSubjectNextMoney(int subjectId,int regionId) throws ServiceException;
   
   int newChildSubjectId(int id) throws ServiceException;
}
