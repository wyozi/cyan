package cyan.views

import org.scalatestplus.play.PlaySpec

class PingTableSpec extends PlaySpec {
  "PingTable.sequentialGrouping" should {
    "group sequentially identical values" in {
      import views.snippet.PingTable.sequentialGrouping

      sequentialGrouping[(Int, String), Int](List(), x => x._1) mustBe List()
      sequentialGrouping[(Int, String), Int](List((1, "abc")), x => x._1) mustBe List((1, List((1, "abc"))))
      sequentialGrouping[(Int, String), Int](List((1, "abc"), (2, "def")), x => x._1) mustBe List((1, List((1, "abc"))), (2, List((2, "def"))))
      sequentialGrouping[(Int, String), Int](List((1, "abc"), (1, "def")), x => x._1) mustBe List((1, List((1, "abc"), (1, "def"))))
    }
  }
}
