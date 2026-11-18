package info.syachiku.global;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PeriodController {
    @GetMapping("/periods/new")
    public String input(Model model) {
        model.addAttribute("periodForm", new PeriodForm());
        return "periods/input";
    }

    @PostMapping("/periods")
    public String submit(@Valid @ModelAttribute("periodForm") PeriodForm form,
                         BindingResult errors) {
        // 個別項目のエラーがある場合、日付同士の比較は行わない。
        if (errors.hasFieldErrors()) {
            return "periods/input";
        }
        if (form.getStartDate().isAfter(form.getEndDate())) {
            errors.reject("period.order", "開始日は終了日以前にしてください");
        }
        if (errors.hasErrors()) {
            return "periods/input";
        }
        // このサンプルは保存を行わず、正常入力の遷移だけを示す。
        return "redirect:/periods/complete";
    }

    @GetMapping("/periods/complete")
    @ResponseBody
    public String complete() { return "入力を確認しました（保存はしません）"; }
}
