package me.jcala.xmarket.mvp.team.add;

import android.content.Context;
import android.widget.EditText;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import me.jcala.xmarket.conf.Api;
import me.jcala.xmarket.data.dto.Result;
import me.jcala.xmarket.data.pojo.Team;
import me.jcala.xmarket.data.storage.UserIntermediate;
import me.jcala.xmarket.util.RetrofitUtils;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class TeamAddPresenterImpl implements TeamAddPresenter,TeamAddModel.OnTeamAddListener{
    private Context context;
    private TeamAddView view;
    private TeamAddModel model;

    public TeamAddPresenterImpl(Context context, TeamAddView view) {
        this.context = context;
        this.view = view;
        this.model=new TeamAddModelImpl();
    }

    @Override
    public void submit(EditText teamTitle, EditText teamDesc, String teamImg, String idImg) {
          Team team=checkForm(teamTitle,teamTitle,teamImg,idImg);
        if (!team.isReleaseCheck()){
            return;
        }
        List<MultipartBody.Part> parts= RetrofitUtils.filesToMultipartBodyParts(Arrays.asList(teamImg,idImg));
        String teamJsonStr=new Gson().toJson(team);
        RequestBody trade=RetrofitUtils.createPartFromString(teamJsonStr);
        model.executeTeamAddReq(this,trade,parts);
    }

    @Override
    public void onComplete(Result<String> result) {
        view.whenStopProgress();
        if (result==null){
            view.whenFail(Api.SERVER_ERROR.msg());
            return;
        }

        switch (result.getCode()) {
            case 100:
                view.whenSuccess();break;
            case 99:
                view.whenFail(Api.SERVER_ERROR.msg());break;
            default:
        }
    }

    private Team checkForm(EditText teamTitle, EditText teamDesc, String teamImg, String idImg){
          Team team=new Team();
          team.setReleaseCheck(false);
          String titleData=teamTitle.getText().toString().trim();
          if (titleData.isEmpty()){
              view.whenFail("志愿队标题不可以为空");
              return team;
         }
          team.setName(titleData);
          String descData=teamDesc.getText().toString().trim();
          if (descData.isEmpty()){
              view.whenFail("志愿队描述不可以为空");
              return team;
          }
         team.setDescription(descData);
          if (teamImg==null||teamImg.isEmpty()){
              view.whenFail("请选择志愿队封面照");
              return team;
          }
        if (idImg==null||idImg.isEmpty()){
            view.whenFail("请上传用于验证的学生照");
            return team;
        }
        String userId= UserIntermediate.instance.getUser(context).getId();
        team.setAuthorId(userId);
        team.setReleaseCheck(true);
        return team;
    }
}