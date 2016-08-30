package board;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;



public class BoardDBBean {
	
	private static BoardDBBean instance = new BoardDBBean();

	public static BoardDBBean getInstance() {
		return instance;
	}

	private BoardDBBean() {
	}

	
	String res = "config.xml";
	SqlSession session  = null;
	
	
	public void insertArticle(BoardDataBean article) throws Exception {
		
		try{
			InputStream is = Resources.getResourceAsStream(res);
			SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is);
			session = factory.openSession();
			
			int num = article.getNum();
			int ref = article.getRef();
			int re_step = article.getRe_step();
			int re_level = article.getRe_level();
			int number = 0;
			
			Integer max = session.selectOne("board.maxBoard");
			if(max != null){
				number = max + 1;
			} else{
				number = 1;
			}
			
			
			if(num != 0){
				
				int up = session.update("board.upBoard",article);
				article.setRe_step(re_step+1);
				article.setRe_level(re_level+1);
				
			} else{
				ref = number;
				re_step = 0;
				re_level = 0;
			}
			
			int i = session.insert("board.addBoard", article);
			
			if(i ==0){
				System.out.println("insert실패");
				}else{
				System.out.println("성공");
				}
			session.commit();
		
			} catch(Exception e){
				System.out.println(e.getMessage());
			}finally{
				session.close();
			}
	}
	
	// list.jsp : 페이징을 위해서 전체 DB에 입력된 행의수가 필요하다...!!!
		public int getArticleCount() throws Exception {
			
			int count=0;
			
			try{
				InputStream is = Resources.getResourceAsStream(res);
				SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is);
				session = factory.openSession();
				
				 count = session.selectOne("board.allCount");
				
				
			}catch(Exception e){
				System.out.println(e.getMessage());
			}finally{
				session.close();
			}
			return count;
		}
		
		// list.jsp ==> Paging!!! DB로부터 여러행을 결과로 받는다.
		public List getArticles(int start, int end) throws Exception {	
			
			List articleList = null;
			try{
				InputStream is = Resources.getResourceAsStream(res);
				SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is);
				session = factory.openSession();
				
				HashMap<String,Integer> map = new HashMap<String, Integer>();
				map.put("start", new Integer(start));
				map.put("end", new Integer(end));
				
				
				articleList = session.selectList("board.pageBoard",map);
				
				
			}catch(Exception e){
				System.out.println(e.getMessage());
			}finally{
				session.close();
			}
			
			return articleList;
		}	
	
		
	
	// read.jsp : DB로부터 한줄의 데이터를 가져온다.
	public BoardDataBean getArticle(int num) throws Exception {
		
		BoardDataBean article = new BoardDataBean();
		try {
			
			InputStream is = Resources.getResourceAsStream(res);
			SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is);
			session = factory.openSession();
			
			Integer x = session.update("board.upRead", num);
			if(x != null){
				System.out.println(article.getReadcount());
				System.out.println(x);
				
				article = session.selectOne("board.conSel", num);
			}
			
			
			session.commit();
		}catch (Exception ex) {
			System.out.println(ex.getMessage());
		} finally {
			session.close();
		}
		return article;
	}
/*
	// updateForm.jsp : 수정폼에 한줄의 데이터를 가져올때.
	public BoardDataBean updateGetArticle(int num) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		BoardDataBean article = null;
		try {
			conn = getConnection();

			pstmt = conn.prepareStatement("select * from board where num = ?");
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
 
			if (rs.next()) {
				article = new BoardDataBean();
				article.setNum(rs.getInt("num"));
				article.setWriter(rs.getString("writer"));
				article.setEmail(rs.getString("email"));
				article.setSubject(rs.getString("subject"));
				article.setPasswd(rs.getString("passwd"));
				article.setReg_date(rs.getTimestamp("reg_date"));
				article.setReadcount(rs.getInt("readcount"));
				article.setRef(rs.getInt("ref"));
				article.setRe_step(rs.getInt("re_step"));
				article.setRe_level(rs.getInt("re_level"));
				article.setContent(rs.getString("content"));
				article.setIp(rs.getString("ip"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ex) {
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ex) {
				}
		}
		return article;
	}

	// updatePro.jsp : 실제 데이터를 수정하는 메소드.
	public int updateArticle(BoardDataBean article) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String dbpasswd = "";
		String sql = "";
		int x = -1;
		try {
			conn = getConnection();

			pstmt = conn.prepareStatement("select passwd from board where num = ?");
			pstmt.setInt(1, article.getNum());
			rs = pstmt.executeQuery();

			if (rs.next()) {
				dbpasswd = rs.getString("passwd");
				if (dbpasswd.equals(article.getPasswd())) {
					sql = "update board set writer=?,email=?,subject=?,passwd=?";
					sql += ",content=? where num=?";
					pstmt = conn.prepareStatement(sql);

					pstmt.setString(1, article.getWriter());
					pstmt.setString(2, article.getEmail());
					pstmt.setString(3, article.getSubject());
					pstmt.setString(4, article.getPasswd());
					pstmt.setString(5, article.getContent());
					pstmt.setInt(6, article.getNum());
					pstmt.executeUpdate();
					x = 1;
				} else {
					x = 0;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ex) {
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ex) {
				}
		}
		return x;
	}

	// deletePro.jsp : 실제 데이터를 삭제하는 메소드...
	public int deleteArticle(int num, String passwd, int ref, int re_level) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;
		String dbpasswd = "";
		int dbref ;
		int dbre_level ;
		int x = -1;
		try {
			conn = getConnection();

			pstmt = conn.prepareStatement("select passwd,ref,re_level from board where num = ?");
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				dbpasswd = rs.getString("passwd");
				dbref = rs.getInt("ref");
				dbre_level = rs.getInt("re_level");
				if (dbpasswd.equals(passwd)) {
					
						pstmt = conn.prepareStatement("delete from board where num>=? and ref=? and re_level>=?");
						pstmt.setInt(1, num);
						pstmt.setInt(2, ref);
						pstmt.setInt(3, re_level);
						pstmt.executeUpdate();
						x = 1; // 글삭제 성공
													
				} else
					x = 0; // 글삭제 실패
				
			} 
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ex) {
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ex) {
				}
		}
		return x;
	}

	 public int getArticleCount(int n, String searchKeyword) throws Exception{
			
			Connection conn = null;
			PreparedStatement pstmt =null;
			ResultSet rs = null;
			
			String[] column_name = {"writer","subject","content"};
			
			int x = 0;
			
			try
			{
				conn = getConnection();
				pstmt = conn.prepareStatement("select count (*) from board where "+column_name[n]+" like '%"+searchKeyword+"%'");
				
				rs = pstmt.executeQuery();
				
				if(rs.next())
					x = rs.getInt(1);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			finally
			{
				if(rs != null) try {rs.close();} catch(SQLException ex){}
				if(pstmt != null) try {pstmt.close();} catch(SQLException ex){}
				if(conn != null) try {conn.close();} catch(SQLException ex){}
			}
			return x;
		}

	 public List getArticles(int start, int end, int n, String searchKeyword) throws Exception
		{
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			List articleList = null;
			
			String[] column_name = {"writer","subject","content"};
			
			try
			{
				conn = getConnection();
				
				String sql = "select num,writer,email,subject,passwd,reg_date,ref,re_step,re_level,content,ip,readcount,r "	
							+ "from (select num,writer,email,subject,passwd,reg_date,ref,re_step,re_level,content,ip,readcount,rownum r "
							+"from (select num,writer,email,subject,passwd,reg_date,ref,re_step,re_level,content,ip,readcount "
							+"from board order by ref desc, re_step asc) where "+column_name[n]+" like '%"+searchKeyword+"%' order by ref desc, re_step asc ) where r >= ? and r <= ?";
				
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, start);
				pstmt.setInt(2,	end);
				
				rs = pstmt.executeQuery();
				
				if(rs.next())
				{
					articleList = new ArrayList(end);
					
					do{
						BoardDataBean article = new BoardDataBean();
						
						article.setNum(rs.getInt("num"));
						article.setWriter(rs.getString("writer"));
						article.setEmail(rs.getString("email"));
						article.setSubject(rs.getString("subject"));
						article.setPasswd(rs.getString("passwd"));
						article.setReg_date(rs.getTimestamp("reg_date"));
						article.setReadcount(rs.getInt("readcount"));
						article.setRef(rs.getInt("ref"));
						article.setRe_step(rs.getInt("re_step"));
						article.setRe_level(rs.getInt("re_level"));
						article.setContent(rs.getString("content"));
						article.setIp(rs.getString("ip"));
						
						
						articleList.add(article);
					}while(rs.next());
					
				}
				
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			finally
			{
				if(rs != null) try {rs.close();} catch(SQLException ex){}
				if(pstmt != null) try {pstmt.close();} catch(SQLException ex){}
				if(conn != null) try {conn.close();} catch(SQLException ex){}
			}
			
			return articleList;
		}
	// search.jsp
	public int selectArticle(String writer, String subject, String content) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String dbwriter = "";
		String dbsubject = "";
		String dbcontent = "";
		int x = -1;
		try {
			conn = getConnection();

			pstmt = conn.prepareStatement("select * from board where writer=like '%?%' or subject=like '%?%' or content=like '%?%'");
			pstmt.setString(1, writer);
			pstmt.setString(2, subject);
			pstmt.setString(3, content);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				dbwriter = rs.getString("writer");
				dbsubject = rs.getString("subject");
				dbcontent = rs.getString("content");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ex) {
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ex) {
				}
		}
		return x;
	}*/
}
