# -*- coding: utf-8 -*-
"""
用 Word COM 更新目录、保存 docx、导出 PDF，并统计页数。

由于工程路径含中文，先把 docx 复制到临时 ASCII 路径处理，再复制回 report。
运行：python tools/postprocess_docx.py
"""
import os
import shutil
import subprocess
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOCX = os.path.join(ROOT, "report", "实验三_详细设计报告.docx")
PDF = os.path.join(ROOT, "report", "实验三_详细设计报告.pdf")

PS = r"""$ErrorActionPreference = 'Stop'
$src = 'C:\Users\lenovo\AppData\Local\Temp\exp3_report.docx'
$pdf = 'C:\Users\lenovo\AppData\Local\Temp\exp3_report.pdf'
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
try {
    $doc = $word.Documents.Open($src, $false, $false)
    $doc.Fields.Update()
    try {
        if ($doc.TablesOfContents.Count -ge 1) {
            $doc.TablesOfContents.Item(1).Update()
        }
    } catch {
        Write-Output "toc-update-skipped"
    }
    $doc.Save()
    $pages = $doc.ComputeStatistics(2)
    $doc.ExportAsFixedFormat($pdf, 17)
    Write-Output "pages=$pages"
    $doc.Close(0)
} finally {
    $word.Quit()
}
"""


def main():
    tmp = tempfile.gettempdir()
    tmp_docx = os.path.join(tmp, "exp3_report.docx")
    tmp_pdf = os.path.join(tmp, "exp3_report.pdf")
    ps_path = os.path.join(tmp, "postprocess_report.ps1")
    shutil.copy2(DOCX, tmp_docx)
    with open(ps_path, "w", encoding="ascii") as fp:
        fp.write(PS)
    result = subprocess.run(
        ["powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", ps_path],
        capture_output=True, text=True
    )
    print(result.stdout.strip())
    if result.returncode != 0:
        print(result.stderr.strip())
        raise SystemExit(result.returncode)
    shutil.copy2(tmp_docx, DOCX)
    shutil.copy2(tmp_pdf, PDF)
    pages = 0
    for line in result.stdout.splitlines():
        if line.startswith("pages="):
            pages = int(line.split("=")[1])
    print("docx:", DOCX)
    print("pdf :", PDF)
    print("pages:", pages)
    if pages < 15:
        raise SystemExit("页数不足 15 页")


if __name__ == "__main__":
    main()
