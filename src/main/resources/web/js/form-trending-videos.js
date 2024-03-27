function formatNumber(number) {
    var suffixes = ["", "K", "M", "B", "T"];
    var suffixNum = 0;

    while (number >= 1000) {
        number /= 1000;
        suffixNum++;
    }

    return number.toFixed(1) + suffixes[suffixNum];
}

$(document).ready(function() {

    var page = 1;
    var submitButton = $("#submit-button");
    var showMoreButton = $("#show-more-button");

    var urlParams = new URLSearchParams(window.location.search);

    submitButton.click(function(event) {
        event.preventDefault();

        window.location.href = "/yt_trending_videos?" + "geo=" + $("#geo").val() + "&subscribers_min=" + $("#subscribers-slider-range").slider("values", 0) +
            "&subscribers_max=" + $("#subscribers-slider-range").slider("values", 1) +
            "&views_min=" + $("#views-slider-range").slider("values", 0) +
            "&views_max=" + $("#views-slider-range").slider("values", 1) +
            "&only_trending=" + $('#only-trending-checkbox').prop('checked') +
            "&only_with_links=" + $('#only-with-links-checkbox').prop('checked');
    });

    showMoreButton.click(function(event) {

        event.preventDefault();

        var url = new URL(window.location.href);

        page = parseInt(page) + 1;

        url.searchParams.set("page", page);

        $.get(url.href, function(responseHtml) {

            var tempElement = $("<div></div>");
            tempElement.html(responseHtml);

            var divs = tempElement.find(".card");

            if (divs.length > 0) {

                divs.each(function() {
                    $("#yt-videos-container").append(this);
                });

                $("#show-more-button-container").appendTo("#yt-videos-container");
            } else {
                $("#show-more-button-container").remove();
            }

        });
    });

    $("#subscribers-slider-range").slider({
        range: true,
        min: 0,
        max: 250000000,
        step: 10000,
        values: [0, 10000000],
        slide: function(event, ui) {
            $("#subscribers").text("Количество подписчиков: от " + formatNumber(ui.values[0]) + " до " + formatNumber(ui.values[1]));
        }
    });

    $("#views-slider-range").slider({
        range: true,
        min: 0,
        max: 250000000,
        step: 10000,
        values: [0, 10000000],
        slide: function(event, ui) {
            $("#views").text("Количество просмотров: от " + formatNumber(ui.values[0]) + " до " + formatNumber(ui.values[1]));
        }
    });

    if (urlParams.has("geo") || urlParams.has("only_trending")) {

        var geoParam = urlParams.get("geo");
        var onlyTrendingParam = urlParams.get("only_trending");
        var onlyWithLinksParam = urlParams.get("only_with_links");
        var subscribersMinParam = urlParams.get("subscribers_min");
        var subscribersMaxParam = urlParams.get("subscribers_max");
        var viewsMinParam = urlParams.get("views_min");
        var viewsMaxParam = urlParams.get("views_max");

        $("#geo").val(geoParam);
        $("#only-trending-checkbox").prop("checked", onlyTrendingParam === "true");
        $("#only-with-links-checkbox").prop("checked", onlyWithLinksParam === "true");

        $("#subscribers-slider-range").slider({
            values: [subscribersMinParam, subscribersMaxParam]
        });

        $("#views-slider-range").slider({
            values: [viewsMinParam, viewsMaxParam]
        });
    }

    $("#subscribers").text("Количество подписчиков: от " + formatNumber($("#subscribers-slider-range").slider("values", 0)) +
        " до " + formatNumber($("#views-slider-range").slider("values", 1)));

    $("#views").text("Количество просмотров: от " + formatNumber($("#views-slider-range").slider("values", 0)) +
        " до " + formatNumber($("#views-slider-range").slider("values", 1)));
});