console.log("global script in featured site")

$hooks.registerAction("system/scheduler/register", (context) => {
	context.arguments().get("scheduler").schedule(
			"0/10 * * * * ?", 
			"test-cron-job-1", 
			(context) => {
				console.log("job 1")
			})
})
$hooks.registerAction("system/scheduler/register", (context) => {
	context.arguments().get("scheduler").schedule(
			"0/10 * * * * ?", 
			"test-cron-job-2", 
			(context) => {
				console.log("job 2")
			})
})
$hooks.registerAction("system/scheduler/register", (context) => {
	context.arguments().get("scheduler").schedule(
			"0/10 * * * * ?", 
			"test-cron-job-3", 
			(context) => {
				console.log("job 3")
			})
})
$hooks.registerAction("system/scheduler/remove", (arguments) => {
	context.arguments().get("scheduler").remove("test-cron-job-1")
	context.arguments().get("scheduler").remove("test-cron-job-2")
	context.arguments().get("scheduler").remove("test-cron-job-3")
})