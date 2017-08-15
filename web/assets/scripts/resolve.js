/**
 * Created by Alien on 2017/7/31.
 */
(function () {
    (function bindListener() {
        $(".day").click(() => getRels());
    })();
    function getRels() {
        const files = $('#xFile').prop('files');
        console.log(files[0]);
        if(files) {
            const form = new FormData();
            form.append('from', $('.from1').val());
            form.append('to', $('.to1').val());
            form.append('file', files[0]);
            $.ajax({
                url: "/GenDayRel",
                type: "POST",
                data: form,
                processData: false,  // 不处理数据
                contentType: false,   // 不设置内容类型
                success: () => console.log('生成关系文件[天]成功！')
            });
        } else {
            alert('请上传聚类文件！');
        }
    }
})();