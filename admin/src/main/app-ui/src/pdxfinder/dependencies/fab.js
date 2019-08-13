//toggleFab();

//Fab click
$('#prime').click(function() {
    toggleFab();
});

//Toggle chat and links
function toggleFab() {
    $('.prime').toggleClass('is-active');
    $('#prime').toggleClass('is-float');
    $('.fab').toggleClass('is-visible');

}

// Ripple effect
var target, ink, d, x, y;
$(".fab").click(function(e) {
    target = $(this);
    //create .ink element if it doesn't exist
    if (target.find(".ink").length == 0)
        target.prepend("<span class='ink'></span>");

    ink = target.find(".ink");
    //incase of quick double clicks stop the previous animation
    ink.removeClass("animate");

    //set size of .ink
    if (!ink.height() && !ink.width()) {
        //use parent's width or height whichever is larger for the diameter to make a circle which can cover the entire element.
        d = Math.max(target.outerWidth(), target.outerHeight());
        ink.css({
            height: d,
            width: d
        });
    }

    //get click coordinates
    //logic = click coordinates relative to page - parent's position relative to page - half of self height/width to make it controllable from the center;
    x = e.pageX - target.offset().left - ink.width() / 2;
    y = e.pageY - target.offset().top - ink.height() / 2;

    //set the position and add class .animate
    ink.css({
        top: y + 'px',
        left: x + 'px'
    }).addClass("animate");
});