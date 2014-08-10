function init() 
{
	var myMap = new ymaps.Map('map', 
    {
            center: [55.76, 37.64],
            zoom: 13,
            type: 'yandex#map',
            behaviors: ['scrollZoom', 'drag'],
            controls: []
    });
    
    searchStartPoint = new ymaps.control.SearchControl(
    {
        options: 
        {
            useMapBounds: true,
            noPlacemark: true,
            noPopup: true,
            placeholderContent: 'Start here',
            size: 'large',
            float: 'none',
            position: { left: 10, top: 44 }
        }
    });
    
    searchFinishPoint = new ymaps.control.SearchControl(
    {
        options: {
            useMapBounds: true,
            noCentering: true,
            noPopup: true,
            noPlacemark: true,
            placeholderContent: 'End here',
            size: 'large',
            float: 'none',
            position: { left: 10, top: 88 }
        }
    });

    var myButton = new ymaps.control.Button(
    {
        data:
        {
            // Текст на кнопке.
            content: 'Save',
            // Текст всплывающей подсказки.
            title: 'Click to save your route'
        },
        options:
        {
            // Зададим опции для кнопки.
            selectOnClick: false
        }
    });

	myButton.events.add('press', function ()
	{
		route = this.controls.get('routeEditor').getRoute();
		if(route.getWayPoints().getLength() == 0)
		{	
			alert('Enter route points');
			return;
		}
		var coords = [];
		// Получаем массив путей.
        for (var i = 0; i < route.getPaths().getLength(); i++)
        {
            way = route.getPaths().get(i);
            segments = way.getSegments();
            for (var j = 0; j < segments.length; j++) 
            {
            	$.merge(coords, segments[j].getCoordinates());
            }
        }
        
        var name=prompt('Name the route', "");

        
        var s = JSON.stringify(coords);
        $.post("InputServlet", { 'points[]': JSON.stringify(coords), 'name': name});
	}, myMap);    
    
    myMap.controls.add('routeEditor');
    myMap.controls.add('geolocationControl');
    
    //myMap.controls.add(searchStartPoint);
    //myMap.controls.add(searchFinishPoint);
    myMap.controls.add(myButton, {float: "left"});
        
    myMap.controls.get('routeEditor').select();

    ymaps.geolocation.get({
        // Выставляем опцию для определения положения по ip
        provider: 'auto',
        // Карта автоматически отцентрируется по положению пользователя.
        mapStateAutoApply: true
    }).then(function (result)
    {
        myMap.geoObjects.add(result.geoObjects);
    });
}

ymaps.ready(init);