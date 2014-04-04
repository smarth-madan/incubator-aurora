
var API_URL = "rest/auroraclient";

var tasks = [];

var jobObj = {
	priority: 0,
	task: new Object(),
	name: 'job1',
	environment: 'prod',
	max_task_failures: 1,
	enable_hooks: false,
	cluster: 'example',
	production: false,
	role: 'knagireddy'
};

var taskObj = {
		processes: [], //Array of process objects	
    name: 'process1',
    finalization_wait: 30,
		max_failures: 1,
    max_concurrency: 0,
    resources: {
      disk: 134217728,
      ram: 134217728,
      cpu: 1
    },
		constraints: [
			{
        order: [] //Array of process names
			}
		]
};

var processObj = {
	daemon: false,
	name: '',
	ephemeral: false,
	max_failures: 1,
	min_duration: 5,
	cmdline: '',
	final: false
};


$("#createJob").submit(function (event) {
    event.preventDefault();
    daily = false;
    callCreateJob();
    return false;
});

function getExecConfig() {
	jobObj.task = taskObj;
	
	var childCount = 1;
	var newProcesses = [];
	$('#commandContainer').children('').each(function () {
		var newProcess = Object.create(processObj);
		newProcess.name = 'process' + childCount++;
		newProcess.cmdline=$(this).find('.txtCommand').val();

		// Need to do this 'cos of JS inheritence
		var newProcessStr = JSON.stringify(flatten(newProcess));
		var newProcessObj = eval("("+ newProcessStr  + ")");
		newProcesses.push(newProcessObj);
	});

	taskObj.processes = newProcesses;
	taskObj.constraints[0].order=[];
	for (var i = 0; i < newProcesses.length; i++) {
		taskObj.constraints[0].order.push(newProcesses[i].name);
	}
	
	return JSON.stringify(jobObj);
}

function flatten(obj) {
    var result = Object.create(obj);
    for(var key in result) {
        result[key] = result[key];
    }
    return result;
}

var callCreateJob = function () {
    var aSchedulerAddr = $("#aSchedulerAddr").val();
    var aSchedulerPort = $("#aSchedulerPort").val();
    var jobName = $("#jobName").val();
    var environment = $("#environment").val();
    var cpu = $("#cpu").val();
    var ram = $("#ram").val();
    var disk = $("#disk").val();
//    var execConfig = $("#execConfig").val();
		var execConfig = getExecConfig();
		if (!execConfig && execConfig.length == 0) {
			alert('There is no command to process');
			return;
		}
    var create_job_url = API_URL + "/createjob?aSchedulerAddr=" + aSchedulerAddr + "&aSchedulerPort=" + aSchedulerPort + "&" +
        "jobName=" + jobName + "&environment=" + environment + "&cpu=" + cpu + "&ram=" + ram + "&disk=" + disk + "&execConfig=" + execConfig;

    $.ajax({
        url: create_job_url,
        method: "POST",
        success: function (data) {
            $("#result").text(data);
            var link="http://"+aSchedulerAddr+":8081/scheduler/knagireddy/"+environment+"/"+jobName;
            $("#linkToJob").html("<a href="+link+" target=\"_blank\">"+link+"</a>");
        },
        error: function (chr, data, error) {
            attDailyJson = data;
        },
        complete: function (xhr, textStatus) {
            attDailyJson = textStatus;
        }
    }).done(function (data, textStatus, xhr) {
        if (console && console.log) {
            console.log("Sample of data:", data);
        }
    });
}

    function addCommand(ele) {
        var commandFragment = $("#commandTemplate").html();

        if ($('#commandContainer').children().length == 0) {
            $("#commandContainer").append(commandFragment);
        } else {
            $(ele).parent().after(commandFragment);
        }

        $('.btnRemoveCommand').unbind('click');
        $('.btnRemoveCommand').click(function () {
            $(this).parent().remove();
            if ($('#commandContainer').children().length == 0) {
                $('#btnNewCommand').show();
            }
        });

        $('.btnAddCommand').unbind('click');
        $('.btnAddCommand').click(function () {
            addCommand(this);
        });

        $('#btnNewCommand').hide();
				$('#commandContainer').last().find('.txtCommand').focus();
    }

    //All the form filling javascripts.
$(document).ready(function () {

    $('#btnNewCommand').click(function () {
        addCommand(this);
    });
});