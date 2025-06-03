[#import "/libs/header.ftl" as header]
[#import "/libs/navigation.ftl" as nav]
<html>

<head>
	<title>
		${title}
	</title>
	[@header.bootstrap /]
</head>

<body>
	<div class="menu">
		[@nav.main /]
	</div>
	${content}
	<div>
		[#assign x = "something"]
		<h3>
			${x}
		</h3>
		${indexOf("met", x)}
	</div>
	<p>
		[@upper]
		hallo leute
		[/@upper]
	</p>
</body>

</html>