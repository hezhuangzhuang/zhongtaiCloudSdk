package com.hw.cloudlibrary.inter;

import com.huawei.ecterminalsdk.base.TsdkAttendeeBaseInfo;
import com.huawei.ecterminalsdk.base.TsdkBookConfInfo;
import com.huawei.ecterminalsdk.base.TsdkConfLanguage;
import com.huawei.ecterminalsdk.base.TsdkConfMediaType;
import com.huawei.ecterminalsdk.base.TsdkConfRecordMode;
import com.huawei.ecterminalsdk.base.TsdkConfRole;
import com.huawei.ecterminalsdk.base.TsdkConfType;
import com.huawei.ecterminalsdk.base.TsdkContactsInfo;
import com.huawei.ecterminalsdk.models.TsdkManager;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.commonservice.common.common.LocContext;
import com.huawei.opensdk.commonservice.common.util.LogUtil;
import com.huawei.opensdk.demoservice.BookConferenceInfo;
import com.huawei.opensdk.demoservice.ConfConvertUtil;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.huawei.opensdk.demoservice.Member;
import com.huawei.opensdk.loginmgr.LoginMgr;
import com.hw.cloudlibrary.activity.LoadingActivity;
import com.hw.cloudlibrary.ecsdk.common.UIConstants;
import com.hw.cloudlibrary.utils.sharedpreferences.PreferencesHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * author：pc-20171125
 * data:2019/03/18 10:12
 * 登录
 */
public class HuaweiCallImp {
    private static HuaweiCallImp callImp = new HuaweiCallImp();

    public static HuaweiCallImp getInstance() {
        return callImp;
    }

    /**
     * 点对点呼叫
     *
     * @param siteNumber
     * @return
     */
    public long callSite(String siteNumber) {
        LogUtil.i(UIConstants.DEMO_TAG, "callSite:" + siteNumber);
        return CallMgr.getInstance().startCall(siteNumber, true);
    }

    /**
     * 创建会议
     *
     * @param confName
     * @param duration
     * @param memberList
     * @param groupId
     */
    public int createConference(String confName,
                                int duration,
                                List<Member> memberList,
                                String groupId,
                                String startTime,
                                String chairPwd,
                                boolean isInstantConference,
                                boolean isDataConf) {
        BookConferenceInfo bookConferenceInfo = new BookConferenceInfo();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

//        Date date = new Date();
//        String formatStr = dateFormat.format(date);
//        bookConferenceInfo.setStartTime(DateUtil.localTimeUtc(formatStr));
//        bookConferenceInfo.setMediaType(ConfConstant.ConfMediaType.VIDEO_CONF);
        bookConferenceInfo.setMediaType(isDataConf ? TsdkConfMediaType.TSDK_E_CONF_MEDIA_VIDEO_DATA : TsdkConfMediaType.TSDK_E_CONF_MEDIA_VIDEO);
        bookConferenceInfo.setDuration(duration);
        bookConferenceInfo.setSubject("APP_" + groupId + "_" + confName);
//        bookConferenceInfo.setPassWord(chairPwd);
        bookConferenceInfo.setInstantConference(isInstantConference);

        boolean isNull = false;

        if (!isNull) {
            return -1;
        }

        //Join the meeting as chairman
        if (null != LoginMgr.getInstance().getTerminal()) {
            Member chairman = new Member();
            chairman.setNumber(LoginMgr.getInstance().getTerminal());
            chairman.setAccountId(LoginMgr.getInstance().getAccount());
            chairman.setRole(TsdkConfRole.TSDK_E_CONF_ROLE_CHAIRMAN);
            //Other fields are optional, and can be filled according to need
            memberList.add(chairman);
        }

        bookConferenceInfo.setMemberList(memberList);

        int result = MeetingMgr.getInstance().bookConference(bookConferenceInfo);

        if (result != 0) {

        } else {
            //是否自己创建的会议
            PreferencesHelper.saveData(UIConstants.IS_CREATE, true);

            //是否需要自动接听
            PreferencesHelper.saveData(UIConstants.IS_AUTO_ANSWER, true);

            //显示等待界面
            LoadingActivity.startActivty(LocContext.getContext(), confName);
        }
        return result;
    }

    /**
     * @param confName            会议名称
     * @param duration            会议时长
     * @param isInstantConference 是否为即时会议，true:即时
     * @param memberNumbers       参会列表号码
     * @return
     */
    public int createConf(
            String confName,
            long duration,
            List<String> memberNumbers) {
        LogUtil.i(UIConstants.DEMO_TAG, "bookConference.");

        TsdkBookConfInfo bookConfInfo = new TsdkBookConfInfo();
        //是否为及时会议
        boolean isInstantConference = true;
        //是否为及时会议
        if (isInstantConference) {
            bookConfInfo.setConfType(TsdkConfType.TSDK_E_CONF_INSTANT);
            bookConfInfo.setIsAutoProlong(1);
        } else {
            bookConfInfo.setConfType(TsdkConfType.TSDK_E_CONF_RESERVED);
        }

        // 创建会议时设置为高清会议
        bookConfInfo.setIsHdConf(1);
        bookConfInfo.setSubject("APP_" + confName);
        bookConfInfo.setConfMediaType(TsdkConfMediaType.TSDK_E_CONF_MEDIA_VIDEO);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String formatStr = dateFormat.format(date);
        bookConfInfo.setStartTime(formatStr);
        bookConfInfo.setDuration(duration);
        bookConfInfo.setIsAutoRecord(0);
        bookConfInfo.setRecordMode(TsdkConfRecordMode.TSDK_E_CONF_RECORD_DISABLE);

        //TODO：创建参会人员列表
        List<Member> members = getMembers(memberNumbers);

        List<TsdkAttendeeBaseInfo> attendeeList = ConfConvertUtil.memberListToAttendeeList(members);
        bookConfInfo.setAttendeeList(attendeeList);
        bookConfInfo.setAttendeeNum(attendeeList.size());

        //The other parameters are optional, using the default value
        //其他参数可选，使用默认值即可
        bookConfInfo.setLanguage(TsdkConfLanguage.TSDK_E_CONF_LANGUAGE_EN_US);

        int result = TsdkManager.getInstance().getConferenceManager().bookConference(bookConfInfo);
        if (result != 0) {
            LogUtil.e(UIConstants.DEMO_TAG, "bookReservedConf result ->" + result);
            return result;
        } else {
            //是否自己创建的会议
            PreferencesHelper.saveData(UIConstants.IS_CREATE, true);

            //是否需要自动接听
            PreferencesHelper.saveData(UIConstants.IS_AUTO_ANSWER, true);

            //显示等待界面
            LoadingActivity.startActivty(LocContext.getContext(), confName);
        }
        return 0;
    }

    private List<Member> getMembers(List<String> memberNumbers) {
        List<Member> members = new ArrayList<>();
        Member attendee = null;

        for (int i = 0; i < memberNumbers.size(); i++) {
            attendee = new Member();
            attendee.setNumber(memberNumbers.get(i));
            attendee.setDisplayName(memberNumbers.get(i));
            attendee.setRole(TsdkConfRole.TSDK_E_CONF_ROLE_ATTENDEE);
            members.add(attendee);
        }

        //Join the meeting as chairman
        Member chairman = new Member();
        TsdkContactsInfo contactSelf = LoginMgr.getInstance().getSelfInfo();
        if (contactSelf != null) {
            chairman.setDisplayName(contactSelf.getPersonName());
        }
        chairman.setNumber(LoginMgr.getInstance().getTerminal());
        chairman.setAccountId(LoginMgr.getInstance().getAccount());
        chairman.setRole(TsdkConfRole.TSDK_E_CONF_ROLE_CHAIRMAN);
        members.add(chairman);
        return members;
    }

    /**
     * 加入会议
     * 0：未注册
     * <p>
     * 1：注册中
     * <p>
     * 2：注销中
     * <p>
     * 3：已注册
     * <p>
     * 4：无效状态
     *
     * @param accessCode
     */
    public long joinConf(String accessCode) {
//        String state = "";
//        try {
//            state = PreferencesHelper.getData(UIConstants.REGISTER_RESULT_TEMP);
//        } catch (Exception e) {
//        }

//        Toast.makeText(LocContext.getContext(), "state:" + state, Toast.LENGTH_SHORT).show();
//        if ("4".equals(state) || "0".equals(state) || "2".equals(state)) {

        //加入会议
        PreferencesHelper.saveData(UIConstants.JOIN_CONF, true);
        return CallMgr.getInstance().startCall(accessCode, true);
    }
}

