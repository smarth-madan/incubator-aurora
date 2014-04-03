var API_URL =  "http://localhost:8080/incubator-aurora/rest/auroraclient";

$("#createJob").submit(function(event) {
        event.preventDefault();
        daily=false;
        callCreateJob();
        return false;
    });

var callCreateJob = function () {
    var aSchedulerAddr=$("#aSchedulerAddr").val();
    var aSchedulerPort=$("#aSchedulerPort").val();
    var jobName=$("#jobName").val();
    var environment=$("#environment").val();
    var cpu=$("#cpu").val();
    var ram=$("#ram").val();
    var disk=$("#disk").val();
    var execConfig = $("#execConfig").val();
    var create_job_url = API_URL+"/createjob?aSchedulerAddr="+aSchedulerAddr+"&aSchedulerPort="+aSchedulerPort+"&"+
    "jobName="+jobName+"&environment="+environment+"&cpu="+cpu+"&ram="+ram+"&disk="+disk+"&execConfig="+execConfig;

   $.ajax({
        url: create_job_url,
        method: "POST",
        success: function (data) {
            $("#result").text(data);
        },
        error:function(chr,data,error){
            attDailyJson = data;
        },
        complete: function(xhr, textStatus){
            attDailyJson = textStatus;
        }
    }).done(function (data,textStatus,xhr) {
        if( console && console.log ) {
        console.log("Sample of data:", data);
    }});
}

//All the form filling javascripts.
      $(document).ready(function(){
            $("#stageName,#attStageName").select2({
                initSelection : function (element, callback) {
                    var data = {id: DEFAULT_STAGE_NAME, text: DEFAULT_STAGE_NAME};
                    callback(data);
                 },
                ajax: {
                    url: GET_CYCLOPS_URL+"stage",
                    dataType: 'jsonp',
                    quietMillis: 100,
                    data: function (term, page) { // page is the one-based page number tracked by Select2
                    return {
                            type: 'M'
                        };
                    },
                    results: function (data, page) {
                        var stageNames=[];
                        for (var i = 0; i < data.length; i++) {
                            stageNames[i] = {id:data[i].name.split(".")[0], text: data[i].name.split(".")[0]};
                        }
                        return {results: stageNames};
                    },
                    cache:true
                }
            }).change (function(e){
                DEFAULT_STAGE_NAME = e.val;
                getJobNames(DEFAULT_STAGE_NAME);
            });

            $( "#from,#attFrom" ).datepicker({
                defaultDate: "-1w",
                changeMonth: true,
                numberOfMonths: 2,
                onClose: function( selectedDate ) {
                    $( "#to,#attTo" ).datepicker( "option", "minDate", selectedDate );
                },
                maxDate: "+0D"
            });
            $( "#to,#attTo" ).datepicker({
                    defaultDate: "+0w",
                    changeMonth: true,
                    numberOfMonths: 2,
                    onClose: function( selectedDate ) {
                $( "#from,#attFrom" ).datepicker( "option", "maxDate", selectedDate );
                },
                maxDate: "+0D"
            });

            $("#jobNames").select2({
                placeholder: "select one or more job names",
                multiple:true,
                width : "resolve",
                data : []
            });

            $("#format").select2({
                placeholder: "",
                width : "resolve",
                data : [{id:"daily", text:"daily"},{id:"weekly",text:"weekly"}],
            });

            var getJobNames = function (stageName) {
            $.ajax({
                    url: GET_CYCLOPS_URL+"metrics/att/jobnames/"+DEFAULT_STAGE_NAME,
                    method: 'GET',
                    dataType: 'jsonp',
                    success: setJobNames
                });
            }

            function setJobNames(jsonData) {
                var data =[];
                for (var i = 0; i < jsonData.length; i++) {
                    var jobName = jsonData[i];
                    if(jsonData[i].split("-").length > 1){
                        data[i] = {id:jsonData[i],text:jsonData[i].split("-")[1]};
                    }else{
                        data[i] = {id:jsonData[i],text:jsonData[i]};
                    }
                }
                $('#jobNames').select2({
                    placeholder: "select one or more job names",
                    data : data,
                    multiple:true,
                    val:"",
                    width : "80%"
                });
            }

            getJobNames(DEFAULT_STAGE_NAME);
            $("#stageName ,#attStageName").select2("val",DEFAULT_STAGE_NAME);
            $("#format").select2("val","daily");
            $( "#from,#attFrom" ).datepicker("setDate",-7);
            $( "#to,#attTo" ).datepicker("setDate",new Date());
            var toDate =  $("#to").datepicker("getDate");
            toDate.setDate(toDate.getDate()+1);
            $("#chart_div").html('<img src="../../img/spinner.gif" alt="Wait" />');
            $("#fusion_chart_div").html('<img src="../../img/spinner.gif" alt="Wait" />');
            getATT(DEFAULT_STAGE_NAME, $("#attFrom").datepicker("getDate") , toDate);
            getDailyattDailyJson(DEFAULT_STAGE_NAME, $("#from").datepicker("getDate") , toDate, "");
      });